import React, { Component, createRef } from 'react'
import '@smartstyles/smartdetect.less'
import { Button, DatePicker, Icon, Input, message, Modal, Upload } from 'antd';
import CounterDown from './component/counterdown'
import TableList from '../../components/tableList/tableList';
import SmartUpload from './component/upload'
import WarpImageWebGL from './component/drawWarpedImage'
import { regExpConfig } from '../../configs/regular.config';
import { getData, postData, uploadFile } from '../../service/http';
import md5 from 'js-md5';
import api from '../../service/api';
import * as fileutils from '../../utils/fileutils';

const { RangePicker } = DatePicker

import { SMART_TOKEN, SMART_PHONE } from '../../service/http';
import { color } from 'echarts/lib/export';
import moment from 'moment';

export default class home extends Component {
  // 初始化页面常量 绑定事件方法
  constructor(props, context) {
    super(props);
    this.registCounterRef = createRef()
    this.modifyPhoneCounterRef = createRef()
    this.smartUploadFrontRef = createRef()
    this.smartUploadBackRef = createRef()
    this.state = {
      isLogined: false,
      showLoginBox: false,
      showRegisterBox: false,
      userInfo: {
        head: '',
        nick: '',
        phone: '',
        token: '',
      },
      resetModifyUserForm: {
        nick: '',
        head: '',
        phone: '',
        modifyPhone: '',
        phoneCode: '',
        newpwd: '',
        oldpwd: '',
        repwd: '',
      },
      modifyUserForm: {},
      isMenuOpen: false,
      menuItems: [
        { index: 0, label: '我的评分' },
        { index: 1, label: '账户信息' },
        { index: 2, label: '退出登录' },
      ],
      showAccountDialog: false,
      showModifyPwdDialog: false,
      showModifyPhoneDialog: false,
      showScoreRecordDialog: false,
      scoreRecordColumns: [
        {
          title: '评级编号',
          dataIndex: 'certNumber',
          width: 200,
        },
        {
          title: '评级时间',
          dataIndex: 'createdate',
          width: 150,
          render: text => moment(text).format('YYYY-MM-DD HH:mm:ss'),
        },
        {
          title: '表面',
          width: 80,
          dataIndex: 'surface',
        }, {
          title: '居中',
          width: 80,
          dataIndex: 'center',
        },
        {
          title: '边缘',
          width: 80,
          dataIndex: 'corner',
        },
        {
          title: '角落',
          width: 80,
          dataIndex: 'edge',
        },
        {
          title: '操作',
          width: 100,
          render: (text, item) => {
            if (item.status === 1) {
              return <a onClick={() => this.openScoreDetail(item)}>详情</a>
            } else if (item.status === 0) {
              return <span>审核中</span>
            } else if (item.status === 2) {
              return <span style={{ color: 'red' }}>已拒审</span>
            }
          },
        },
      ],
      scoreSearchForm: {
        certNumber: '',
        startTime: '',
        endTime: '',
        dates: [],
      },
      scoreRecordList: [],
      scoreRecordPagination: {
        page: 1,
        pageSize: 10,
        totalCount: 0,
      },
      showScoreDetailDialog: false,
      scoreDetail: {},
      showUploadFrontDialog: false,
      showUploadBackDialog: false,
      showWrapDrawDialog: false,
      loginForm: {
        phone: '',
        pwd: '',
      },
      registForm: {
        phone: '',
        phoneCode: '',
        pwd: '',
      },
      uploadForm: {
        frontUrl: '',
        backUrl: '',
      },
      showFullShadow: false,
    }
  }

  componentDidMount() {
    const smartToken = localStorage.getItem(SMART_TOKEN);
    if (smartToken) {
      // 检测是否还在线
      postData('smartGetUserInfo', {}).then((data) => {
        if (data) {
          this.setState({
            isLogined: true,
            userInfo: data,
          })
        }
      }).catch((err) => {
        this.setState({
          isLogined: false,
          userInfo: {},
        })
      })
    }
  }

  showLoginBoxArea() {
    this.setState({
      showLoginBox: true,
      showRegisterBox: false,
    })
  }

  showRegisterBoxArea() {
    this.setState({
      showLoginBox: false,
      showRegisterBox: true,
    })
  }

  renderLoginBox() {
    return (
      <div
        className="login-box"
        onMouseEnter={() => this.handleMouseEnterLoginBox()}
        // onMouseLeave={() => this.handleMouseLeaveLoginBox()}
      >
        <div className="login-box-title">密码登录</div>
        <Input
          className="base-input login-phone-input"
          placeholder="手机号"
          type="number"
          maxLength={11}
          value={this.state.loginForm.phone}
          onChange={e => this.setState({ loginForm: Object.assign(this.state.loginForm, { phone: e.target.value }) })}
        />
        <Input
          className="base-input login-pwd-input"
          placeholder="登入密码"
          type="password"
          value={this.state.loginForm.pwd}
          onChange={e => this.setState({ loginForm: Object.assign(this.state.loginForm, { pwd: e.target.value }) })}
        />
        <Button className="base-btn login-box-btn" onClick={() => this.clickLogin()}>登录</Button>

        <Icon type="close-circle"
          className="icon-login-close"
          onClick={() => this.handleMouseLeaveLoginBox()}
        />
      </div>
    )
  }

  renderRegisterBox() {
    return (
      <div
        className="register-box"
        onMouseEnter={() => this.handleMouseEnterRegisterBox()}
        // onMouseLeave={() => this.handleMouseLeaveRegisterBox()}
      >
        <div className="register-box-title">账号注册</div>
        <div className="register-phone-box">
          <div className="register-phone-area">+86</div>
          <div className="register-vertical-line" />
          <Input
            className="base-input register-phone-input"
            placeholder="请输入手机号"
            maxLength={11}
            type="number"
            value={this.state.registForm.phone}
            onChange={e => this.setState({ registForm: Object.assign(this.state.registForm, { phone: e.target.value }) })}
          />
        </div>
        <div className="register-code-box">
          <Input
            className="base-input register-code-input"
            type="number"
            maxLength={6}
            placeholder="请输入验证码"
            value={this.state.registForm.phoneCode}
            onChange={e => this.setState({ registForm: Object.assign(this.state.registForm, { phoneCode: e.target.value }) })}
          />
          <div className="register-vertical-line" />
          <CounterDown
            ref={this.registCounterRef}
            onClick={() => this.clickSendPhoneCode(this.state.registForm.phone, 1)}
            onCountdownEnd={() => this.resetCountDown()}
          />
        </div>
        <Input
          className="base-input register-pwd-input"
          placeholder="请输入密码"
          type="password"
          value={this.state.registForm.pwd}
          onChange={e => this.setState({ registForm: Object.assign(this.state.registForm, { pwd: e.target.value }) })}
        />
        <Button className="base-btn register-box-btn" onClick={() => this.clickRegist()}>注册</Button>

        <Icon type="close-circle"
          className="icon-login-close"
          onClick={() => this.handleMouseLeaveRegisterBox()}
        />
      </div>
    )
  }

  renderLoginModal() {
    return (
      <div className="login-btn-box">
        <div
          className="base-btn login-btn"
          onMouseEnter={() => this.handleMouseEnterLoginBox()}
          // onMouseLeave={() => this.handleMouseLeaveLoginBox()}
          onClick={() => this.showLoginBoxArea()}
        >登录</div>
        <div
          className="base-btn register-btn"
          onMouseEnter={() => this.handleMouseEnterRegisterBox()}
          // onMouseLeave={() => this.handleMouseLeaveRegisterBox()}
          onClick={() => this.showRegisterBoxArea()}
        >注册</div>
      </div>
    )
  }

  handleMouseEnterAvatar() {
    if (this.timeout !== undefined) {
      clearTimeout(this.timeout);
    }
    this.setState({ isMenuOpen: true });
  }

  handleMouseLeaveAvatar() {
    this.timeout = setTimeout(() => {
      this.setState({ isMenuOpen: false });
    }, 200);
  }

  handleMenuEnter() {
    if (this.timeout !== undefined) {
      clearTimeout(this.timeout);
    }
  }

  handleMenuLeave() {
    this.timeout = setTimeout(() => {
      this.setState({ isMenuOpen: false });
    }, 100);
  }

    handleMenuItemClick = (menuItem) => {
      if (menuItem.index === 0) {
        // 我的评分弹窗
        this.setState({
          showScoreRecordDialog: true,
        })
        this.getScoreRecord()
      } else if (menuItem.index === 1) {
        // 账号信息弹窗
        this.setState({
          showAccountDialog: true,
          modifyUserForm: Object.assign(this.state.resetModifyUserForm, {
            nick: this.state.userInfo.nick,
            head: this.state.userInfo.head,
            phone: this.state.userInfo.phone,
          }),
        })
      } else if (menuItem.index === 2) {
        // 退出登录
        getData('smartLogout', {}).then((data) => {
          this.setState({
            isLogined: false,
            userInfo: null,
          })
          localStorage.removeItem(SMART_TOKEN)
          localStorage.removeItem(SMART_PHONE)
        })
      }
      // 点击后关闭菜单
      this.setState({ isMenuOpen: false });
    };

    handleMouseEnterLoginBox() {
      if (this.timeout !== undefined) {
        clearTimeout(this.timeout);
      }
      this.setState({ showLoginBox: true, showRegisterBox: false });
    }

    handleMouseLeaveLoginBox() {
      this.timeout = setTimeout(() => {
        this.setState({ showLoginBox: false, loginForm: { phone: '', pwd: '' } });
      }, 200);
    }

    handleMouseEnterRegisterBox() {
      if (this.timeout !== undefined) {
        clearTimeout(this.timeout);
      }
      this.setState({ showRegisterBox: true, showLoginBox: false });
    }

    handleMouseLeaveRegisterBox() {
      this.timeout = setTimeout(() => {
        this.setState({ showRegisterBox: false, registForm: { phone: '', phoneCode: '', pwd: '' } }, () => {
          this.resetCountDown()
        });
      }, 200);
    }

    renderPersonModal() {
      return (
        <div
          className="floating-menu-container"
        >
          {/* 头像区域 */}
          <div className="avatar-container"
            onMouseEnter={() => this.handleMouseEnterAvatar()}
            onMouseLeave={() => this.handleMouseLeaveAvatar()}
          >
            {this.state.userInfo && this.state.userInfo.head ? (
              <img
                src={this.state.userInfo.head}
                alt=""
                className="avatar-image"
              />
            ) : (
              <div className="avatar-placeholder" />
            )}
          </div>

          {/* 悬浮菜单 */}
          <div
            ref="floating-menu"
            className={`floating-menu ${this.state.isMenuOpen ? 'floating-menu-open' : ''}`}
            onMouseEnter={() => this.handleMenuEnter()}
            onMouseLeave={() => this.handleMenuLeave()}
          >
            {this.state.menuItems.map((item, index) => (
              <div
                key={index}
                className="menu-item"
                onClick={() => this.handleMenuItemClick(item)}
              >
                {item.label}
              </div>
            ))}
          </div>
        </div>
      )
    }

    renderHomeBg() {
      return (
        <div className="center-body">
          <div className="center-title">一瞬评级系统</div>
          <div className="center-subtitle">只需拍摄或上传卡片照片，我们将自动识别并给出专业评分，精准评估卡片品质！
          </div>
          <div className="taste-btn" onClick={() => this.clickTaste()}>立即体验</div>
        </div>
      )
    }

    renderAccountDialog() {
      const uploadProps = {
        action: api.smartUploadImg, // 上传地址
        multiple: false,
        name: 'file',
        headers: {
          Authorization: localStorage.getItem('smart-token'),
        },
      };
      const imgProps = {
        onChange: this.uploadImg.bind(this, 'imgUrl'),
        accept: 'image/*',
        listType: 'text',
        showUploadList: false,
        ...uploadProps,
      };
      return (
        <Modal
          visible={this.state.showAccountDialog}
          centered
          closable={false}
          width="800px"
          footer={null}
        >
          <div className="account-dialog-box">
            {this.state.userInfo && this.state.userInfo.head ? (
              <img
                src={this.state.userInfo.head}
                alt=""
                className="set-avatar-image"
              />
            ) : (
              <div className="set-avatar-image default-avatar" />
            )}
            <Upload {...imgProps}>
              <Button className="avatar-upload-btn">上传头像</Button>
            </Upload>
            <div className="account-nick-box">
              <div className="account-nick-label">昵称</div>
              <Input
                className="base-input account-nick-input"
                placeholder="请输入昵称"
                value={this.state.modifyUserForm.nick}
                maxLength={10}
                onChange={e => this.setState({ modifyUserForm: Object.assign(this.state.modifyUserForm, { nick: e.target.value }) })}
              />
              <Button className="base-btn account-nick-save-btn"
                onClick={() => this.modifyNick()}
              >保存</Button>
            </div>

            <div className="account-phone-box">
              <div className="account-phone-label">绑定手机号</div>
              <Input
                className="base-input account-phone-input"
                disabled
                value={this.state.modifyUserForm.phone}
              />
              <Button className="base-btn account-phone-btn" onClick={() => this.setState({ showModifyPhoneDialog: true, showAccountDialog: false, modifyUserForm: Object.assign(this.state.modifyUserForm, { modifyPhone: '' }) })}>更换手机号</Button>
            </div>

            <div className="account-pwd-box">
              <div className="account-pwd-label">登录密码</div>
              <div className="base-input account-pwd-input">
                <Input.Password
                  placeholder="请输入密码"
                  value={this.state.modifyUserForm.newpwd}
                  onChange={e => this.setState({ modifyUserForm: Object.assign(this.state.modifyUserForm, { newpwd: e.target.value }) })}
                />
              </div>
              <Button className="base-btn account-pwd-btn" onClick={() => this.setState({ showModifyPwdDialog: true, showAccountDialog: false, modifyUserForm: Object.assign(this.state.modifyUserForm, { oldpwd: '', repwd: '' }) })}>修改密码</Button>
            </div>

            <Icon type="close-circle"
              className="icon-account-close"
              onClick={() => this.setState({ showAccountDialog: false })}
            />
          </div>
        </Modal>
      )
    }

    renderModifyPwdDialog() {
      return (
        <Modal
          visible={this.state.showModifyPwdDialog}
          centered
          closable
          width="400px"
          footer={null}
          onCancel={() => { this.setState({ showAccountDialog: true, showModifyPwdDialog: false }) }}
        >
          <div className="modifypwd-box">
            <h2>修改密码</h2>
            <div className="modifypwd-item-box">
              <span className="modifypwd-label">原密码</span>
              <div className="base-input modifypwd-input">
                <Input.Password
                  placeholder="请输入原密码"
                  value={this.state.modifyUserForm.oldpwd}
                  onChange={e => this.setState({ modifyUserForm: Object.assign(this.state.modifyUserForm, { oldpwd: e.target.value }) })}
                />
              </div>
            </div>
            <div className="modifypwd-item-box">
              <span className="modifypwd-label">新密码</span>
              <div className="base-input modifypwd-input">
                <Input.Password
                  placeholder="请输入新密码"
                  value={this.state.modifyUserForm.newpwd}
                  onChange={e => this.setState({ modifyUserForm: Object.assign(this.state.modifyUserForm, { newpwd: e.target.value }) })}
                />
              </div>
            </div>
            <div className="modifypwd-item-box">
              <span className="modifypwd-label">确认新密码</span>
              <div className="base-input modifypwd-input">
                <Input.Password
                  placeholder="请确认新密码"
                  value={this.state.modifyUserForm.repwd}
                  onChange={e => this.setState({ modifyUserForm: Object.assign(this.state.modifyUserForm, { repwd: e.target.value }) })}
                />
              </div>
            </div>
            <div className="modifypwd-btn-box">
              <Button className="base-btn modifypwd-confirm-btn" onClick={() => this.modifyPwd()}>修改</Button>
            </div>
          </div>
        </Modal>
      )
    }

    rendermodifyPhoneDialog() {
      return (
        <Modal
          visible={this.state.showModifyPhoneDialog}
          centered
          closable
          width="300px"
          footer={null}
          onCancel={() => this.setState({ showAccountDialog: true, showModifyPhoneDialog: false }, () => {
            // 清除计时器
            if (this.modifyPhoneCounterRef && this.modifyPhoneCounterRef.current) {
              this.modifyPhoneCounterRef.current.reset()
            }
            this.resetCountDown()
          })}
        >
          <div className="modify-phone-box">
            <h2>更换手机号</h2>
            <div className="modify-phone-phone-box">
              <div className="modify-phone-area">+86</div>
              <div className="modify-phone-vertical-line" />
              <Input
                className="base-input modify-phone-input"
                placeholder="请输入手机号"
                maxLength={11}
                type="number"
                value={this.state.modifyUserForm.modifyPhone}
                onChange={e => this.setState({ modifyUserForm: Object.assign(this.state.modifyUserForm, { modifyPhone: e.target.value }) })}
              />
            </div>
            <div className="modify-phone-code-box">
              <Input
                className="base-input modify-phone-code-input"
                type="number"
                maxLength={6}
                placeholder="请输入验证码"
                value={this.state.modifyUserForm.phoneCode}
                onChange={e => this.setState({ modifyUserForm: Object.assign(this.state.modifyUserForm, { phoneCode: e.target.value }) })}
              />
              <div className="modify-phone-vertical-line" />
              <CounterDown
                ref={this.modifyPhoneCounterRef}
                onClick={() => this.clickSendPhoneCode(this.state.modifyUserForm.modifyPhone, 2)}
                onCountdownEnd={() => this.resetCountDown()}
              />
            </div>
            <div className="modify-phone-btn-box">
              <Button className="base-btn modify-phone-confirm-btn"
                onClick={() => this.modifyPhone()}
              >修改</Button>
            </div>
          </div>
        </Modal>
      )
    }

    renderScoreRecordDialog() {
      return (
        <Modal
          visible={this.state.showScoreRecordDialog}
          centered
          closable={false}
          footer={null}
          width={1100}
        >
          <div className="score-record-dialog-box">
            <h1>我的评分</h1>
            <div className="score-record-query-box">
              <div className="score-record-query-label">评级编号</div>
              <Input
                className="base-input score-record-query-input"
                placeholder="请输入"
                value={this.state.scoreSearchForm.certNumber}
                onChange={e => this.setState({ scoreSearchForm: Object.assign(this.state.scoreSearchForm, { certNumber: e.target.value }) })}
              />

              <div className="score-record-query-label">评级时间</div>
              <RangePicker
                value={this.state.scoreSearchForm.dates}
                onChange={(dates, dateStrings) => this.onScoreRecordDateChange(dates, dateStrings)}
                placeholder={['开始日期', '结束日期']}
              />
            </div>

            <div className="score-record-btn-box">
              <Button className="base-btn score-record-query-btn"
                onClick={() => this.getScoreRecord()}
              >查询</Button>
              <Button className="base-btn score-record-reset-btn"
                onClick={() => this.resetScoreSearch()}
              >重置</Button>
            </div>

            <div className="score-record-query-line" />

            <TableList
              tableStyle={this.state.scoreRecordList.length > 0 ? { minHeight: 660 } : { minHeight: 0 }}
              rowKey="id"
              columns={this.state.scoreRecordColumns}
              dataSource={this.state.scoreRecordList}
              currentPage={this.state.scoreRecordPagination.page}
              pageSize={this.state.scoreRecordPagination.pageSize}
              scroll={{ y: true }}
              onChange={this.onScoreRecordPageChange}
              onShowSizeChange={this.onScoreRecordPageSizeChange}
              totalCount={this.state.scoreRecordPagination.totalCount}
            />

            <Icon type="close-circle"
              className="icon-score-record-close"
              onClick={() => this.setState({ showScoreRecordDialog: false })}
            />
          </div>
        </Modal>
      )
    }

    resetScoreSearch() {
      this.setState({
        scoreSearchForm: {
          certNumber: '',
          startTime: '',
          endTime: '',
          dates: [],
        },
      }, () => this.getScoreRecord())
    }

    getScoreRecord() {
      const params = {
        page: this.state.scoreRecordPagination.page,
        pageSize: this.state.scoreRecordPagination.pageSize,
        certNumber: this.state.scoreSearchForm.certNumber || '',
        startTime: this.state.scoreSearchForm.startTime || '',
        endTime: this.state.scoreSearchForm.endTime || '',
      }
      postData('smartGetDetectRecords', params).then((data) => {
        this.setState({
          scoreRecordList: data.list,
          scoreRecordPagination: Object.assign(this.state.scoreRecordPagination, { totalCount: data.totalCount }),
        })
      })
    }

    // 页数改变事件
    onScoreRecordPageChange = (newPage) => {
      this.state.scoreRecordPagination.page = newPage;
      this.getScoreRecord();
    }

    // 页大小改变事件
    onScoreRecordPageSizeChange = (e, pageSize) => {
      this.state.scoreRecordPagination.page = 1
      this.state.scoreRecordPagination.pageSize = pageSize
      this.getScoreRecord()
    }

    renderScoreDetailCardInfo() {
      const detailView = this.state.scoreDetail.detail.map((detail, index) => (
        <div className="score-detail-info-box">
          <div
            className={index === 0 ? 'score-detail-info-nav' : 'score-detail-info-nav score-detail-info-nav-margin'}
          >{detail.type}</div>
          {
            detail.items.map(item => (
              <div>
                <div className="score-detail-info-line" />
                <div className="score-detail-info-item">
                  <div className="score-detail-info-label">{item.label}</div>
                  <div className="score-detail-info-value">{item.value}</div>
                </div>
              </div>
            ))
          }

        </div>

      ))

      return (
        <div>
          {detailView}
        </div>
      )
    }

    renderScoreDetailDialog() {
      return (
        <Modal
          visible={this.state.showScoreDetailDialog}
          centered
          closable={false}
          width={1100}
          footer={null}
        >
          <div className="score-detail-dialog-box">
            <div className="score-detail-title">评级编号</div>
            <div className="score-detail-certnum">{this.state.scoreDetail.certNumber}</div>
            <div className="score-detail-content">
              <div className="score-detail-img-front-box">
                <img src={this.state.scoreDetail.frontUrl}
                  className="score-detail-img-front"
                  alt=""
                />
              </div>
              <div className="score-detail-img-back-box">
                <img src={this.state.scoreDetail.backUrl}
                  className="score-detail-img-back"
                  alt=""
                />
              </div>
              {this.state.scoreDetail.detail && this.state.scoreDetail.status === 1 ? this.renderScoreDetailCardInfo() : null}
              {this.state.scoreDetail.status === 2 ? <h1>已拒绝</h1> : this.state.scoreDetail.status === 0 ? <h1>等待人工审核，请稍后查看</h1> : null}
            </div>
            <Button className="base-btn score-detail-btn"
              onClick={() => {
                this.setState({ showScoreDetailDialog: false })
                if (this.scoreDetailFrom === 'scoreList') {
                  // 回到我的记录
                  this.setState({ showScoreRecordDialog: true })
                }
              }}
            >确认</Button>
          </div>
        </Modal>
      )
    }

    onScoreRecordDateChange(dates, dateStrings) {
      this.setState({
        scoreSearchForm: Object.assign(this.state.scoreSearchForm, {
          dates: dates,
          startTime: String(new Date(`${dateStrings[0]} 00:00:00`).getTime()),
          endTime: String(new Date(`${dateStrings[1]} 23:59:59`).getTime()),
        }),
      })
    }

    openScoreDetail(item) {
      this.queryScoreDetail(item.id)
      this.setState({
        showScoreRecordDialog: false,
        showScoreDetailDialog: true,
      })
      this.scoreDetailFrom = 'scoreList'
    }

    renderUploadDialog(step) {
      return (
        <Modal
          visible={step === 1 ? this.state.showUploadFrontDialog : this.state.showUploadBackDialog}
          centered
          closable
          onCancel={() => this.setState({ showUploadFrontDialog: false, showUploadBackDialog: false })}
          width={window.innerHeight * 1.35}
          footer={null}
          zIndex={300}
        >
          <SmartUpload
            ref={step === 1 ? this.smartUploadFrontRef : this.smartUploadBackRef}
            key={step === 1 ? 'frontUpload' : 'backUpload'}
            step={step}
            drawWrap={(url, wstep, imgRealWidth, imgRealHeight) => this.openDrawWrapDialog(url, wstep, imgRealWidth, imgRealHeight)}
            goNext={(wstep, imgUrl) => this.uploadNext(wstep, imgUrl)}
            goBack={() => {
              this.setState({
                showUploadBackDialog: false,
                showUploadFrontDialog: true,
              })
            }}
          />
        </Modal>
      )
    }

    uploadNext(step, imgUrl) {
      if (step === 1) {
        // 打开
        this.setState({
          showUploadBackDialog: true,
          showUploadFrontDialog: false,
          uploadForm: Object.assign(this.state.uploadForm, { frontUrl: imgUrl }),
        })
      } else {
        this.setState({
          showUploadBackDialog: false,
          uploadForm: Object.assign(this.state.uploadForm, { backUrl: imgUrl }),
        }, () => {
          this.detectCard()
        })
      }
    }

    openDrawWrapDialog(url, step, imgRealWidth, imgRealHeight) {
      this.setState({
        wrapUrl: url,
        wrapStep: step,
        beforeWrapImgWidth: imgRealWidth,
        beforeWrapImgHeight: imgRealHeight,
        showWrapDrawDialog: true,
      })
    }

    getWrapImgSize(wrapWidth, wrapHeight) {
      const r1000height = window.innerHeight
      // 尺寸缩放到指定比例内
      if (wrapWidth > r1000height * 0.4 || wrapHeight > r1000height * 0.6) {
        return this.getWrapImgSize(wrapWidth * 0.9, wrapHeight * 0.9)
      }
      return { imgWidth: wrapWidth, imgHeight: wrapHeight }
    }

    renderWrapDrawDialog() {
      console.log(`realImgWidth:${this.state.beforeWrapImgWidth},realImgHeight:${this.state.beforeWrapImgHeight}`)
      const { imgWidth, imgHeight } = this.getWrapImgSize(this.state.beforeWrapImgWidth, this.state.beforeWrapImgHeight)
      console.log(`imgWidth:${imgWidth},imgHeight:${imgHeight}`)
      return (
        <Modal
          visible={this.state.showWrapDrawDialog}
          centered
          closable
          onCancel={() => this.setState({ showWrapDrawDialog: false })}
          width={imgWidth + 200}
          footer={null}
          zIndex={999}
        >
          <div className="wrapdraw-box">
            {
              this.state.showWrapDrawDialog ? (
                <WarpImageWebGL
                  imageUrl={this.state.wrapUrl}
                  imgWidth={imgWidth}
                  imgHeight={imgHeight}
                  onSave={urlData => this.saveWrapDraw(urlData)}
                />
              ) : null
            }

          </div>

        </Modal>
      )
    }

    saveWrapDraw(urlData) {
      const file = fileutils.base64ToFile(urlData, 'warped.png');
      uploadFile('smartUploadCardImg', { type: this.state.wrapStep }, file).then((data) => {
        message.success('保存成功')
        this.setState({
          showWrapDrawDialog: false,
        }, () => {
          if (this.state.wrapStep === 1) {
            if (this.smartUploadFrontRef && this.smartUploadFrontRef.current) {
              this.smartUploadFrontRef.current.updateWrapImg(data)
            }
          } else if (this.smartUploadBackRef && this.smartUploadBackRef.current) {
            this.smartUploadBackRef.current.updateWrapImg(data)
          }
        })
      }).catch((err) => {
        message.error(err.message || '保存失败')
      })
    }

    clickTaste() {
      if (this.state.isLogined) {
        if (this.smartUploadFrontRef && this.smartUploadFrontRef.current) {
          this.smartUploadFrontRef.current.updateWrapImg(null)
        }
        if (this.smartUploadBackRef && this.smartUploadBackRef.current) {
          this.smartUploadBackRef.current.updateWrapImg(null)
        }
        this.setState({
          showUploadFrontDialog: true,
          uploadForm: {
            frontUrl: '',
            backUrl: '',
          },
        })
      } else {
        this.setState({
          showLoginBox: true,
          showRegisterBox: false,
        })
      }
    }

    clickLogin() {
      if (!this.state.loginForm.phone) {
        message.error('请输入手机号');
        return;
      }
      if (!regExpConfig.mobile.test(this.state.loginForm.phone)) {
        message.error('手机号格式不正确');
        return;
      }
      if (!this.state.loginForm.pwd) {
        message.error('请输入密码');
        return;
      }
      if (this.state.loginForm.pwd.length < 6) {
        message.error('密码长度不能少于6位');
        return;
      }
      const params = {
        phone: this.state.loginForm.phone,
        password: md5(this.state.loginForm.pwd),
      }
      postData('smartLogin', params).then((data) => {
        if (data && data.token) {
          localStorage.setItem(SMART_TOKEN, data.token);
          localStorage.setItem(SMART_PHONE, data.phone);
          this.setState({
            userInfo: data,
            isLogined: true,
            showLoginBox: false,
          });
          message.success('登录成功')
        } else {
          message.error('登录失败');
        }
      }).catch((err) => {
        message.error(err.message || '登录失败');
      });
    }

    clickSendPhoneCode(phone, scene) {
      if (this.isSending || this.isCounting) {
        return
      }

      if (!phone) {
        message.error('请先输入手机号')
        return
      }
      if (!regExpConfig.mobile.test(phone)) {
        message.error('手机号格式不正确')
        return
      }
      this.isSending = true
      const params = {
        phone: phone,
        scene: scene,
      }
      postData('smartSendCode', params).then((data) => {
        // 倒计时
        if (scene === 1) {
          if (this.registCounterRef && this.registCounterRef.current) {
            this.registCounterRef.current.startCountdown(60)
          }
        } else if (scene === 2) {
          if (this.modifyPhoneCounterRef && this.modifyPhoneCounterRef.current) {
            this.modifyPhoneCounterRef.current.startCountdown(60)
          }
        }
        this.isSending = false
        this.isCounting = true
      }).catch((err) => {
        message.error(err.message || '发送失败');
        this.isSending = false
      });
    }

    resetCountDown() {
      this.isCounting = false
      this.isSending = false
    }

    clickRegist() {
      if (!this.state.registForm.phone) {
        message.error('请输入手机号');
        return;
      }
      if (!regExpConfig.mobile.test(this.state.registForm.phone)) {
        message.error('手机号格式不正确');
        return;
      }
      if (!this.state.registForm.phoneCode) {
        message.error('请输入验证码');
        return;
      }
      if (!(/\d{4}$/).test(this.state.registForm.phoneCode)) {
        message.error('请输入正确的验证码');
        return;
      }
      if (!this.state.registForm.pwd) {
        message.error('请输入密码');
        return;
      }
      if (this.state.registForm.pwd.length < 6) {
        message.error('密码长度不能少于6位');
        return;
      }
      const params = {
        phone: this.state.registForm.phone,
        verifyCode: this.state.registForm.phoneCode,
        password: md5(this.state.registForm.pwd),
      }
      postData('smartRegist', params).then((data) => {
        if (data && data.token) {
          localStorage.setItem(SMART_TOKEN, data.token);
          localStorage.setItem(SMART_PHONE, data.phone);
          // 清除计时器
          if (this.registCounterRef && this.registCounterRef.current) {
            this.registCounterRef.current.reset()
          }
          this.setState({
            isLogined: true,
            userInfo: data,
            showRegisterBox: false,
          })
          this.resetCountDown()
          message.success('注册成功')
        } else {
          message.error('注册失败');
        }
      }).catch((err) => {
        message.error(err.message || '注册失败');
      });
    }

    modifyNick() {
      const nick = this.state.modifyUserForm.nick.trim().toString()
      if (!nick) {
        message.error('请输入昵称');
        return
      }
      postData('smartModifyNick', { nick }).then((data) => {
        message.success('修改成功')
        this.setState({
          userInfo: Object.assign(this.state.userInfo, { nick }),
        })
      }).catch((err) => {
        message.error(err.message || '修改失败')
      })
    }

    modifyPwd() {
      const newpwd = this.state.modifyUserForm.newpwd.trim().toString()
      const oldpwd = this.state.modifyUserForm.oldpwd.trim().toString()
      const repwd = this.state.modifyUserForm.repwd.trim().toString()
      if (!oldpwd) {
        message.error('请输入原密码');
        return
      }
      if (oldpwd.length < 6) {
        message.error('原密码长度不能少于6位');
        return
      }
      if (!newpwd) {
        message.error('请输入新密码');
        return
      }
      if (newpwd.length < 6) {
        message.error('新密码长度不能少于6位');
        return
      }
      if (!repwd) {
        message.error('请输入确认密码');
        return
      }
      if (repwd.length < 6) {
        message.error('确认密码长度不能少于6位');
        return
      }
      if (newpwd !== repwd) {
        message.error('二次密码不一致');
        return
      }
      postData('smartModifyPwd', { newpwd: md5(newpwd), oldpwd: md5(oldpwd) }).then((data) => {
        message.success('修改成功')
        this.setState({
          showAccountDialog: true,
          showModifyPwdDialog: false,
        })
      }).catch((err) => {
        message.error(err.message || '修改失败')
      })
    }

    modifyPhone() {
      const phone = this.state.modifyUserForm.modifyPhone.trim().toString()
      const phoneCode = this.state.modifyUserForm.phoneCode.trim().toString()
      if (!phone) {
        message.error('请输入手机号');
        return
      }
      if (!regExpConfig.mobile.test(phone)) {
        message.error('手机号格式不正确');
        return;
      }
      if (!phoneCode) {
        message.error('请输入验证码');
        return;
      }
      if (!(/\d{4}$/).test(phoneCode)) {
        message.error('请输入正确的验证码');
        return;
      }
      const params = {
        phone: phone,
        verifyCode: phoneCode,
      }
      postData('smartModifyPhone', params).then((data) => {
        message.success('修改成功')
        // 清除计时器
        if (this.modifyPhoneCounterRef && this.modifyPhoneCounterRef.current) {
          this.modifyPhoneCounterRef.current.reset()
        }
        this.resetCountDown()
        this.setState({
          showAccountDialog: true,
          showModifyPhoneDialog: false,
          modifyUserForm: Object.assign(this.state.modifyUserForm, { phone: phone }),
          userInfo: Object.assign(this.state.userInfo, { phone: phone }),
        })
      }).catch((err) => {
        message.error(err.message || '修改失败')
      })
    }

    detectCard() {
      if (!this.state.uploadForm.frontUrl) {
        message.error('请上传正面图片')
        return
      }
      if (!this.state.uploadForm.backUrl) {
        message.error('请上传反面图片')
        return
      }
      message.loading('正在检测，请稍等...', 3 * 60000)
      this.setState({
        showFullShadow: true,
      })
      const params = {
        frontUrl: this.state.uploadForm.frontUrl,
        backUrl: this.state.uploadForm.backUrl,
      }
      postData('smartDetectCard', params, 300000).then((data) => {
        message.destroy()
        this.setState({
          showScoreDetailDialog: true,
          showFullShadow: false,
        })
        this.scoreDetailFrom = 'detect'
        this.parseScoreDetail(data)
      }).catch((err) => {
        message.destroy()
        Modal.error({
          content: err.message || '检测失败',
        })
        // message.error(err.message || '检测失败')
        this.setState({
          showUploadBackDialog: true,
          showScoreDetailDialog: false,
          showFullShadow: false,
        })
      })
    }

    renderFullShadow() {
      return this.state.showFullShadow ? (
        <div style={{
          width: '100%', height: '100%', background: '#33000000', zIndex: 99999, position: 'absolute',
        }}
          onClick={e => e.preventDefault()}
        />
      ) : null
    }

    queryScoreDetail(id) {
      message.loading('正在查询，请稍等...', 3 * 60000)
      postData('smartQueryDetectDetail', { id }).then((data) => {
        message.destroy()
        this.parseScoreDetail(data)
      }).catch((err) => {
        message.destroy()
        message.error(err.message || '查询失败')
      })
    }

    parseScoreDetail(data) {
      this.setState({
        scoreDetail: {
          certNumber: data.certNumber,
          frontUrl: data.frontUrl,
          backUrl: data.backUrl,
          status: data.status,
          detail: data.status === 1 ? [
            {
              type: '卡牌信息',
              items: [
                { label: '卡牌种类', value: data.cardInfo.cardType },
                { label: '系列、卡包', value: data.cardInfo.title ? `${data.cardInfo.brand}：${data.cardInfo.title}` : data.cardInfo.brand },
                { label: '名称', value: data.cardInfo.name },
                { label: '稀有度', value: data.cardInfo.rarity },
              ],
            },
            {
              type: '卡牌评分',
              items: [
                { label: '表面', value: data.surface },
                { label: '居中', value: data.center },
                { label: '边缘', value: data.edge },
                { label: '角落', value: data.corner },
              ],
            },
          ] : [],
        },
      })
    }

    uploadImg = (type, info) => {
      let fileList = [...info.fileList];
      // 1. Limit the number of uploaded files
      // Only to show one recent uploaded files, and old ones will be replaced by the new
      fileList = fileList.slice(-1);
      // 2. Read from response and show file link
      fileList = fileList.map((file) => {
        if (file.response) {
          // Component will show file.url as link
          file.url = file.response.url;
          file.name = file.response.name;
        }
        return file;
      });
      if (type === 'imgUrl' && fileList && fileList.length > 0 && fileList[0].url) {
        this.modifyHead(fileList[0].url)
      }
    };

    async modifyHead(url) {
      postData('smartModifyHead', { head: url }).then((data) => {
        message.success('更新成功')
        this.setState({
          userInfo: Object.assign(this.state.userInfo, { head: url }),
        })
      })
    }

    render() {
      return (
        <div className="home-box">
          <div className="header-box">
            <div className="header-title">YISHUN</div>
          </div>
          <div className="nav-box">
            <div className="logo" />
            {this.state.isLogined ? this.renderPersonModal() : this.renderLoginModal()}
          </div>
          {this.state.showLoginBox ? this.renderLoginBox() : null}
          {this.state.showRegisterBox ? this.renderRegisterBox() : null}
          {this.renderHomeBg()}
          {this.renderAccountDialog()}
          {this.renderModifyPwdDialog()}
          {this.rendermodifyPhoneDialog()}
          {this.renderScoreRecordDialog()}
          {this.renderScoreDetailDialog()}
          {this.renderUploadDialog(1)}
          {this.renderUploadDialog(2)}
          {this.renderWrapDrawDialog()}
          {this.renderFullShadow()}
          <div className="footer-container">
            {/* <Footer/> */}
          </div>
        </div>
      )
    }
}
