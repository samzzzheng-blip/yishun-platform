// WarpImageClass.jsx
import React, { Component, createRef } from 'react';
import { Button } from 'antd';

/**
 * WarpImageClass
 * Props:
 *  - imageUrl: string (图片 URL，需跨域允许 CORS 或同域)
 *  - width: number (CSS 宽度，px)
 *  - height: number (CSS 高度, px)
 *  - defaultPoints: [[x,y],...] 四个点 (以 CSS 像素为单位，顺序：TL, TR, BR, BL)
 *  - onPointsChange(pointsInCssPx): optional callback when mouse up (points in CSS px)
 *
 * Methods available on instance (父组件通过 ref 调用):
 *  - exportImage(): returns dataUrl PNG (包含透明背景)
 *  - resetPoints(): reset to defaultPoints
 *  - getPoints(): returns current points in CSS pixels
 *
 * Implementation notes:
 *  - Uses two canvases overlay: one WebGL for texture & homography, one 2D for handles/interaction.
 *  - Homography (dest -> src) solved by small linear system (8x8) per render.
 */
export default class WarpImageClass extends Component {
  constructor(props) {
    super(props);
    this.bgCanvasRef = createRef();
    this.glCanvasRef = createRef(); // WebGL draw canvas (pixel buffer, exported)
    this.ovlCanvasRef = createRef(); // 2D overlay canvas (handles, mouse capture)

    // devicePixelRatio aware: internal points stored in device pixels
    const DPR = typeof window !== 'undefined' ? window.devicePixelRatio || 1 : 1;
    this.DPR = DPR;

    const w = props.imgWidth || 400;
    const h = props.imgHeight || 300;

    // convert defaultPoints (CSS px) -> device px
    const dp = p => [Math.round((p[0] || 0) * DPR), Math.round((p[1] || 0) * DPR)];
    const defaults = props.defaultPoints || [
      [50, 50],
      [w - 50, 50],
      [w - 50, h - 50],
      [50, h - 50],
    ];
    this.points = defaults.map(dp); // stored in device pixels

    this.state = {
      draggingIndex: -1,
      isLoaded: false,
    };

    // internal refs
    this.gl = null;
    this.program = null;
    this.positionBuffer = null;
    this.texture = null;
    this.image = null;

    // RAF handle
    this.queuedFrame = false;

    // bind
    this.handlePointerDown = this.handlePointerDown.bind(this);
    this.handlePointerMove = this.handlePointerMove.bind(this);
    this.handlePointerUp = this.handlePointerUp.bind(this);
    this.exportImage = this.exportImage.bind(this);
    this.resetPoints = this.resetPoints.bind(this);
    this.getPoints = this.getPoints.bind(this);
  }

  componentDidMount() {
    // set canvas pixel sizes with DPR and CSS sizes
    const cssW = this.props.imgWidth || 400;
    const cssH = this.props.imgHeight || 300;
    const DPR = this.DPR;

    const bgCanvas = this.bgCanvasRef.current;
    const glCanvas = this.glCanvasRef.current;
    const ovlCanvas = this.ovlCanvasRef.current;

    // 绘制网格背景
    bgCanvas.width = Math.round(cssW * DPR);
    bgCanvas.height = Math.round(cssH * DPR);
    bgCanvas.style.width = `${cssW}px`;
    bgCanvas.style.height = `${cssH}px`;
    const context = bgCanvas.getContext('2d');
    context.save();
    context.strokeStyle = 'gray';
    context.lineWidth = 0.5;
    const stepX = 20
    const stepY = 20
    for (let i = stepX + 0.5; i < context.canvas.width; i += stepX) {
      context.beginPath();
      context.moveTo(i, 0);
      context.lineTo(i, context.canvas.height);
      context.stroke();
    }

    for (let i = stepY + 0.5; i < context.canvas.height; i += stepY) {
      context.beginPath();
      context.moveTo(0, i);
      context.lineTo(context.canvas.width, i);
      context.stroke();
    }
    context.restore();

    // set actual pixel buffer sizes
    glCanvas.width = Math.round(cssW * DPR);
    glCanvas.height = Math.round(cssH * DPR);
    glCanvas.style.width = `${cssW}px`;
    glCanvas.style.height = `${cssH}px`;

    ovlCanvas.width = Math.round(cssW * DPR);
    ovlCanvas.height = Math.round(cssH * DPR);
    ovlCanvas.style.width = `${cssW}px`;
    ovlCanvas.style.height = `${cssH}px`;

    // init WebGL
    this.initGL();

    // init overlay events
    ovlCanvas.addEventListener('pointerdown', this.handlePointerDown, { passive: false });
    window.addEventListener('pointermove', this.handlePointerMove, { passive: false });
    window.addEventListener('pointerup', this.handlePointerUp, { passive: false });

    // load image
    if (this.props.imageUrl) this.loadImage(this.props.imageUrl);
  }

  componentWillUnmount() {
    const ovl = this.ovlCanvasRef.current;
    if (ovl) ovl.removeEventListener('pointerdown', this.handlePointerDown);
    window.removeEventListener('pointermove', this.handlePointerMove);
    window.removeEventListener('pointerup', this.handlePointerUp);
  }

  // Public methods
  exportImage() {
    // export the WebGL canvas as PNG (transparent background)
    const el = this.glCanvasRef.current;
    try {
      const urlData = el.toDataURL('image/png');
      if (this.props.onSave) {
        this.props.onSave(urlData)
      }
    } catch (e) {
      console.error('Export failed. CORS or context issue?', e);
      return null;
    }
  }

  resetPoints() {
    const cssW = this.props.imgWidth || 400;
    const cssH = this.props.imgHeight || 300;
    const DPR = this.DPR;
    const defaults = this.props.defaultPoints || [
      [50, 50],
      [cssW - 50, 50],
      [cssW - 50, cssH - 50],
      [50, cssH - 50],
    ];
    this.points = defaults.map(p => [p[0] * DPR, p[1] * DPR]);
    this.requestRender();
  }

  getPoints() {
    // return in CSS pixels (more convenient for parent)
    const DPR = this.DPR;
    return this.points.map(p => [p[0] / DPR, p[1] / DPR]);
  }

  // Initialize WebGL program and buffers
  initGL() {
    const canvas = this.glCanvasRef.current;
    const gl = canvas.getContext('webgl', { premultipliedAlpha: false, preserveDrawingBuffer: true });
    if (!gl) {
      console.error('WebGL not supported');
      return;
    }
    this.gl = gl;
    // vertex shader: receive pixel-space position, convert to clip space,
    // pass pixel-space position to fragment shader as v_pos
    const vsSource = `
      attribute vec2 a_position;
      uniform vec2 u_resolution;
      varying vec2 v_pos;
      void main() {
        vec2 zeroToOne = a_position / u_resolution;
        vec2 clip = zeroToOne * 2.0 - 1.0;
        gl_Position = vec4(clip * vec2(1.0, -1.0), 0, 1);
        v_pos = a_position;
      }
    `;
    // fragment shader: apply H (dest->src) to map dest pixel to src pixel,
    // then sample texture with normalized uv = src / imageSize
    const fsSource = `
      precision mediump float;
      uniform mat3 u_Hinv; // dest -> src homography
      uniform sampler2D u_image;
      uniform vec2 u_imageSize;
      varying vec2 v_pos;
      void main() {
        vec3 d = vec3(v_pos, 1.0);
        vec3 s = u_Hinv * d;
        // avoid division by zero
        if (abs(s.z) < 1e-6) discard;
        vec2 sp = s.xy / s.z; // src pixel coordinates
        vec2 uv = vec2(sp.x / u_imageSize.x, sp.y / u_imageSize.y);
        // if outside, keep transparent
        if (uv.x < 0.0 || uv.x > 1.0 || uv.y < 0.0 || uv.y > 1.0) discard;
        gl_FragColor = texture2D(u_image, uv);
      }
    `;

    const vs = this._createShader(gl.VERTEX_SHADER, vsSource);
    const fs = this._createShader(gl.FRAGMENT_SHADER, fsSource);
    const program = this._createProgram(vs, fs);
    this.program = program;

    // look up locations
    this.aPositionLoc = gl.getAttribLocation(program, 'a_position');
    this.uResolutionLoc = gl.getUniformLocation(program, 'u_resolution');
    this.uImageSizeLoc = gl.getUniformLocation(program, 'u_imageSize');
    this.uHinvLoc = gl.getUniformLocation(program, 'u_Hinv');
    this.uImageLoc = gl.getUniformLocation(program, 'u_image');

    // buffers
    this.positionBuffer = gl.createBuffer();

    // enable alpha blending so areas outside quad are transparent
    gl.enable(gl.BLEND);
    gl.blendFunc(gl.SRC_ALPHA, gl.ONE_MINUS_SRC_ALPHA);
  }

  _createShader(type, source) {
    const gl = this.gl;
    const shader = gl.createShader(type);
    gl.shaderSource(shader, source);
    gl.compileShader(shader);
    if (!gl.getShaderParameter(shader, gl.COMPILE_STATUS)) {
      const info = gl.getShaderInfoLog(shader);
      gl.deleteShader(shader);
      throw new Error(`Shader compile failed: ${info}`);
    }
    return shader;
  }

  _createProgram(vs, fs) {
    const gl = this.gl;
    const p = gl.createProgram();
    gl.attachShader(p, vs);
    gl.attachShader(p, fs);
    gl.linkProgram(p);
    if (!gl.getProgramParameter(p, gl.LINK_STATUS)) {
      const info = gl.getProgramInfoLog(p);
      gl.deleteProgram(p);
      throw new Error(`Program link failed: ${info}`);
    }
    return p;
  }

  // Load image and create texture
  loadImage(url) {
    const img = new Image();
    img.crossOrigin = 'anonymous';
    img.onload = () => {
      this.image = img;
      // create / update texture
      this._createOrUpdateTexture();
      // initial render
      this.setState({ isLoaded: true }, () => this.requestRender());
    };
    img.onerror = (e) => {
      console.error('Image load error:', e);
    };
    img.src = url;
  }

  _createOrUpdateTexture() {
    const gl = this.gl;
    if (!gl || !this.image) return;
    if (!this.texture) this.texture = gl.createTexture();
    gl.bindTexture(gl.TEXTURE_2D, this.texture);
    // flip Y so that image pixel (0,0) is top-left in our src coords
    // gl.pixelStorei(gl.UNPACK_FLIP_Y_WEBGL, true);
    gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, gl.RGBA, gl.UNSIGNED_BYTE, this.image);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);
    gl.bindTexture(gl.TEXTURE_2D, null);
  }

  // Schedule a render via requestAnimationFrame (throttled)
  requestRender() {
    if (this.queuedFrame) return;
    this.queuedFrame = true;
    window.requestAnimationFrame(() => {
      this.queuedFrame = false;
      this._renderGL();
      this._drawOverlay();
    });
  }

  // Compute homography H such that: [src] ~= H * [dest]   (dest -> src)
  // destPts: [[x,y] ...]   srcPts: [[X,Y] ...]
  computeHomography(destPts, srcPts) {
    // Build 8x8 system A*h = b
    // h = [h11,h12,h13,h21,h22,h23,h31,h32]^T
    const A = [];
    const b = [];
    for (let i = 0; i < 4; i++) {
      const x = destPts[i][0];
      const y = destPts[i][1];
      const X = srcPts[i][0];
      const Y = srcPts[i][1];
      A.push([x, y, 1, 0, 0, 0, -x * X, -y * X]);
      b.push(X);
      A.push([0, 0, 0, x, y, 1, -x * Y, -y * Y]);
      b.push(Y);
    }
    const h = this._gaussSolve(A, b);
    if (!h) return null;
    // assemble 3x3 matrix row-major
    const H = [
      [h[0], h[1], h[2]],
      [h[3], h[4], h[5]],
      [h[6], h[7], 1],
    ];
    return H;
  }

  // Solve linear system Ax=b by Gaussian elimination
  _gaussSolve(A, b) {
    // A: n x n, b: n
    const n = A.length;
    if (n === 0) return null;
    const m = A[0].length;
    // Build augmented matrix M (n x (m+1)), expecting m == n == 8
    const M = new Array(n);
    for (let i = 0; i < n; i++) {
      M[i] = new Array(m + 1);
      for (let j = 0; j < m; j++) M[i][j] = A[i][j];
      M[i][m] = b[i];
    }

    for (let col = 0; col < m; col++) {
      // pivot
      let pivot = col;
      for (let r = col + 1; r < n; r++) {
        if (Math.abs(M[r][col]) > Math.abs(M[pivot][col])) pivot = r;
      }
      if (Math.abs(M[pivot][col]) < 1e-12) return null; // singular
      // swap
      if (pivot !== col) {
        const tmp = M[col];
        M[col] = M[pivot];
        M[pivot] = tmp;
      }
      // normalize row col
      const div = M[col][col];
      for (let c = col; c <= m; c++) M[col][c] /= div;
      // eliminate others
      for (let r = 0; r < n; r++) {
        if (r === col) continue;
        const factor = M[r][col];
        for (let c = col; c <= m; c++) M[r][c] -= factor * M[col][c];
      }
    }
    // extract solution
    const sol = new Array(m);
    for (let i = 0; i < m; i++) sol[i] = M[i][m];
    return sol;
  }

  // Convert row-major 3x3 H (H[row][col]) to Float32Array in column-major required by WebGL
  _mat3ToGL(H) {
    // Column-major order: [H00,H10,H20, H01,H11,H21, H02,H12,H22]
    return new Float32Array([
      H[0][0], H[1][0], H[2][0],
      H[0][1], H[1][1], H[2][1],
      H[0][2], H[1][2], H[2][2],
    ]);
  }

  // The core GL render: draw full-screen geometry and map pixels via homography
  _renderGL() {
    const gl = this.gl;
    const canvas = this.glCanvasRef.current;
    if (!gl || !this.image) return;

    const width = canvas.width; // device pixels
    const height = canvas.height;
    gl.viewport(0, 0, width, height);
    gl.clearColor(0, 0, 0, 0); // transparent background
    gl.clear(gl.COLOR_BUFFER_BIT);

    gl.useProgram(this.program);

    // positions: full-screen two triangles, in pixel coordinates
    const positions = new Float32Array([
      0, 0,
      width, 0,
      width, height,
      0, 0,
      width, height,
      0, height,
    ]);
    // 倒置
    // const positions = new Float32Array([
    //   0, height,
    //   width, height,
    //   0, 0,
    //   width, height,
    //   width, 0,
    //   0, 0,
    // ]);
    gl.bindBuffer(gl.ARRAY_BUFFER, this.positionBuffer);
    gl.bufferData(gl.ARRAY_BUFFER, positions, gl.STATIC_DRAW);
    gl.enableVertexAttribArray(this.aPositionLoc);
    gl.vertexAttribPointer(this.aPositionLoc, 2, gl.FLOAT, false, 0, 0);

    // u_resolution
    gl.uniform2f(this.uResolutionLoc, width, height);
    // u_imageSize: image pixel dims (not scaled)
    gl.uniform2f(this.uImageSizeLoc, this.image.width, this.image.height);

    // compute Hinv: dest -> src, where dest points are in device pixels
    // src rect is the full image [0,0]..[imgW,imgH] (top-left origin)
    const destPts = this.points; // device px
    const srcPts = [
      [0, 0],
      [this.image.width, 0],
      [this.image.width, this.image.height],
      [0, this.image.height],
    ];
    // 倒置
    // const srcPts = [
    //   [0, this.image.height],
    //   [this.image.width, this.image.height],
    //   [this.image.width, 0],
    //   [0, 0]
    // ];
    let H = this.computeHomography(destPts, srcPts);
    if (!H) {
      // fallback: simple scale mapping dest -> src by image / canvas
      const sx = this.image.width / width;
      const sy = this.image.height / height;
      H = [
        [sx, 0, 0],
        [0, sy, 0],
        [0, 0, 1],
      ];
    }
    const hArr = this._mat3ToGL(H);
    gl.uniformMatrix3fv(this.uHinvLoc, false, hArr);

    // bind texture
    gl.activeTexture(gl.TEXTURE0);
    gl.bindTexture(gl.TEXTURE_2D, this.texture);
    gl.uniform1i(this.uImageLoc, 0);

    // draw
    gl.drawArrays(gl.TRIANGLES, 0, 6);
  }

  // Draw overlay handles on separate 2D canvas (device pixels)
  _drawOverlay() {
    const canvas = this.ovlCanvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    const w = canvas.width;
    const h = canvas.height;
    ctx.clearRect(0, 0, w, h);

    // draw polygon outline
    ctx.lineWidth = Math.max(2, 2 * this.DPR);
    ctx.strokeStyle = 'rgba(96,165,250,0.9)'; // blue
    ctx.beginPath();
    const p0 = this.points[0];
    ctx.moveTo(p0[0], p0[1]);
    for (let i = 1; i < 4; i++) ctx.lineTo(this.points[i][0], this.points[i][1]);
    ctx.closePath();
    ctx.stroke();

    // draw handles
    const r = Math.max(6 * this.DPR, 6);
    this.points.forEach((p, idx) => {
      // outer border
      ctx.beginPath();
      ctx.arc(p[0], p[1], r + 2, 0, Math.PI * 2);
      ctx.fillStyle = 'rgba(255,255,255,0.95)';
      ctx.fill();
      // inner
      ctx.beginPath();
      ctx.arc(p[0], p[1], r, 0, Math.PI * 2);
      ctx.fillStyle = idx === this.state.draggingIndex ? 'rgba(34,197,94,1)' : 'rgba(59,130,246,1)';
      ctx.fill();
      // small center
      ctx.beginPath();
      ctx.arc(p[0], p[1], Math.max(2 * this.DPR, 1), 0, Math.PI * 2);
      ctx.fillStyle = '#fff';
      ctx.fill();
    });
  }

  // Pointer events on overlay (we use pointer to handle mouse + touch)
  handlePointerDown(e) {
    e.preventDefault();
    const ovl = this.ovlCanvasRef.current;
    const rect = ovl.getBoundingClientRect();

    const cssX = e.clientX - rect.left;
    const cssY = e.clientY - rect.top;
    const x = cssX * this.DPR;
    const y = cssY * this.DPR;

    // find nearest handle within threshold
    const th = Math.max(12 * this.DPR, 10);
    let chosen = -1;
    for (let i = 0; i < 4; i++) {
      const dx = this.points[i][0] - x;
      const dy = this.points[i][1] - y;
      if (Math.hypot(dx, dy) <= th) {
        chosen = i;
        break;
      }
    }
    if (chosen >= 0) {
      this.setState({ draggingIndex: chosen });
      this.isDragging = true;
      // store last pointer id for touch multi pointer safety
      this.pointerId = e.pointerId;
    } else {
      // if not clicking on handle: could implement dragging whole quad, but we skip
      this.setState({ draggingIndex: -1 });
      this.isDragging = false;
    }
  }

  handlePointerMove(e) {
    if (!this.isDragging) return;
    // only handle same pointer
    if (this.pointerId != null && e.pointerId !== this.pointerId) return;
    e.preventDefault();
    const ovl = this.ovlCanvasRef.current;
    const rect = ovl.getBoundingClientRect();
    const cssX = e.clientX - rect.left;
    const cssY = e.clientY - rect.top;
    const x = cssX * this.DPR;
    const y = cssY * this.DPR;

    const idx = this.state.draggingIndex;
    if (idx >= 0) {
      // clamp inside canvas
      const nx = Math.min(Math.max(0, x), ovl.width);
      const ny = Math.min(Math.max(0, y), ovl.height);
      this.points[idx][0] = nx;
      this.points[idx][1] = ny;
      // schedule render
      this.requestRender();
    }
  }

  handlePointerUp(e) {
    if (!this.isDragging) return;
    // only handle same pointer
    if (this.pointerId != null && e.pointerId !== this.pointerId) {
      // ignore
    }
    this.isDragging = false;
    this.pointerId = null;
    const idx = this.state.draggingIndex;
    this.setState({ draggingIndex: -1 }, () => {
      // notify parent with CSS-px points
      if (typeof this.props.onPointsChange === 'function') {
        const cssPoints = this.getPoints();
        try { this.props.onPointsChange(cssPoints); } catch (err) { console.error(err); }
      }
    });
  }

  render() {
    // the two canvases sit in a relative container; overlay is on top
    const cssW = this.props.imgWidth || 400;
    const cssH = this.props.imgHeight || 300;
    const containerStyle = { position: 'relative', width: `${cssW}px`, height: `${cssH}px`, userSelect: 'none' };

    // export methods are instance methods, parent can call via ref
    return (
      <div>
        <div style={containerStyle}>
          <canvas
            ref={this.bgCanvasRef}
            style={{ display: 'block', position: 'absolute', left: 0, top: 0 }}
            width={Math.round(cssW * this.DPR)}
            height={Math.round(cssH * this.DPR)}
          />
          <canvas
            ref={this.glCanvasRef}
            style={{ display: 'block', position: 'absolute', left: 0, top: 0 }}
            width={Math.round(cssW * this.DPR)}
            height={Math.round(cssH * this.DPR)}
          />
          <canvas
            ref={this.ovlCanvasRef}
            style={{
              display: 'block', position: 'absolute', left: 0, top: 0, touchAction: 'none',
            }}
            width={Math.round(cssW * this.DPR)}
            height={Math.round(cssH * this.DPR)}
          />
        </div>
        <div style={{ marginTop: 8 }}>
          <Button className="base-btn upload-btnUpload" onClick={() => this.exportImage()}>保存</Button>
          <Button className="base-btn upload-btnUpload" onClick={() => this.resetPoints()} style={{ marginLeft: 8 }}>重置</Button>
        </div>
      </div>
    );
  }
}
