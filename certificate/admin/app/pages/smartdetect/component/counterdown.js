import React, { Component } from 'react';
import PropTypes from 'prop-types';
import '@smartstyles/counterdown.less'

class CounterDown extends Component {
  constructor(props) {
    super(props);
    this.state = {
      countdown: props.initialCount, // 倒计时秒数
    };
    this.timer = null; // 定时器引用
  }

    // 提供给外部调用的方法：开始倒计时
    startCountdown = (duration = 60) => {
      this.setState({ countdown: duration }, () => {
        this.runCountdown()
      });
    };

    // 执行倒计时逻辑
    runCountdown = () => {
      this.timer = setInterval(() => {
        this.setState((prevState) => {
          if (prevState.countdown <= 1) {
            clearInterval(this.timer);
            this.timer = null;
            this.props.onCountdownEnd && this.props.onCountdownEnd();
            return { countdown: 0 };
          }
          return { countdown: prevState.countdown - 1 };
        });
      }, 1000);
    };

    // 提供给外部调用的方法：重置倒计时
    reset = () => {
      if (this.timer) {
        clearInterval(this.timer);
        this.timer = null;
      }
      this.setState({ countdown: 0 });
    };

    // 处理按钮点击事件
    handleClick = () => {
      if (this.state.countdown > 0) return;

      // 首先调用外部传入的onClick方法
      if (this.props.onClick) {
        this.props.onClick();
      } else {
        // 如果没有提供onClick方法，则直接开始倒计时
        this.startCountdown();
      }
    };

    // 组件卸载时清除定时器
    componentWillUnmount() {
      this.reset()
    }

    render() {
      const { countdown } = this.state;
      const { className, label, disabled } = this.props;
      const isButtonDisabled = disabled || countdown > 0;

      // 按钮文本
      const buttonText = countdown > 0
        ? `${countdown}s`
        : label || '发送验证码';

      return (
        <button
          onClick={this.handleClick}
          disabled={isButtonDisabled}
          className={`counter-down-button ${isButtonDisabled ? 'disabled' : ''} ${className || ''}`}
        >
          {buttonText}
        </button>
      );
    }
}

// 定义组件属性类型
CounterDown.propTypes = {
  // 自定义按钮文本
  label: PropTypes.string,

  // 点击后触发的回调函数
  onClick: PropTypes.func,

  // 倒计时结束时触发的回调
  onCountdownEnd: PropTypes.func,

  // 初始倒计时的值（用于恢复状态）
  initialCount: PropTypes.number,

  // 外部控制按钮禁用状态
  disabled: PropTypes.bool,

  // 自定义类名
  className: PropTypes.string,
};

// 默认属性
CounterDown.defaultProps = {
  label: '发送验证码',
  disabled: false,
  initialCount: 0,
};


export default CounterDown;
