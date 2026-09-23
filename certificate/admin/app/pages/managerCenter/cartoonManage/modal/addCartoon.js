
import React, { Component } from 'react'
import { Button, Form, Input, message, Upload, Icon, Modal, DatePicker } from 'antd'
import { postData } from '@http';
import moment from 'moment';

const FormItem = Form.Item;
import api from '@service/api';

@Form.create({})

export default class Index extends Component {
  constructor(props) {
    super(props)
    let imgName,
      evidenceName,
      evidenceVideoName;
    if (this.props.values) {
      imgName = this.props.values.imgUrl ? this.props.values.imgUrl.substring(this.props.values.imgUrl.lastIndexOf('\/') + 1) : null;
      evidenceName = this.props.values.evidenceImg ? this.props.values.evidenceImg.substring(this.props.values.evidenceImg.lastIndexOf('\/') + 1) : null;
      evidenceVideoName = this.props.values.evidenceVideo ? this.props.values.evidenceVideo.substring(this.props.values.evidenceVideo.lastIndexOf('\/') + 1) : null;
    }
    this.state = {
      loading: false,
      imgList: imgName ? [
        {
          uid: '1',
          status: 'done',
          name: imgName,
          url: this.props.values.imgUrl,
        },
      ] : [],
      evidenceImgList: evidenceName ? [
        {
          uid: '2',
          status: 'done',
          name: evidenceName,
          url: this.props.values.evidenceImg,
        },
      ] : [],
      evidenceVideoList: evidenceVideoName ? [
        {
          uid: '3',
          status: 'done',
          name: evidenceVideoName,
          url: this.props.values.evidenceVideo,
        },
      ] : [],
    };
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  // 组件已经加载到dom中
  componentDidMount() {
    this.props.form.resetFields()
  }

  handleSubmit(e) {
    e.preventDefault()
    this.props.form.validateFields((errors, values) => {
      if (errors) {
        return;
      }
      values.imgUrl = values.imgUrl.fileList.length > 0 ? values.imgUrl.fileList[0].url : '';
      values.evidenceImg = values.evidenceImg.fileList.length > 0 ? values.evidenceImg.fileList[0].url : '';
      values.evidenceVideo = values.evidenceVideo.fileList.length > 0 ? values.evidenceVideo.fileList[0].url : '';

      this.setState({ loading: true }, () => {
        if (this.props.type === 'edit') {
          // 更新
          const params = {
            ...values,
            id: this.props.values.id,
          }

          postData('updateCartoon', params).then((data) => {
            message.success('更新成功');
            this.props.handleOk && this.props.handleOk();
            this.setState({ loading: false });
          }).catch((err) => {
            message.warning(err.message);
            this.setState({ loading: false });
          });
        } else {
          // 新增
          postData('addCartoon', values).then((data) => {
            message.success('新增成功');
            this.setState({ loading: false });
            this.props.handleOk && this.props.handleOk();
          }).catch((err) => {
            this.setState({ loading: false });
            message.warning(err.message);
          });
        }
      })
    })
  }

  footer() {
    return (
      <div>
        <Button type="primary" onClick={this.handleSubmit} loading={this.state.loading}>确定</Button>
        <Button onClick={this.props.onCancel}>取消</Button>
      </div>
    )
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
        if (type === 'evidenceVideo') {
          this.props.form.setFieldsValue({
            evidenceVideoImgUrl: file.response.videoPicUrl,
          });
        }
      }
      return file;
    });
    if (type === 'imgUrl') {
      this.setState({ imgList: fileList });
    } else if (type === 'evidenceImg') {
      this.setState({ evidenceImgList: fileList });
    } else if (type === 'evidenceVideo') {
      this.setState({ evidenceVideoList: fileList });
    }
  };

  choosePublishTime = (value) => {
    this.props.form.setFieldsValue({
      publishTime: `${moment(value, 'YYYY-MM-DD HH:mm:ss').valueOf()}`,
    });
  };

  handleRemove = (type, file) => {
    if (type === 'evidenceVideo') {
      this.props.form.setFieldsValue({
        evidenceVideoImgUrl: '',
      });
    }
  };

  checkFileUpload = (rule, value, callback) => {
    if (value.fileList.length > 0) {
      return callback();
    }
    callback('不能为空');
  };

  render() {
    const {
      visible, onCancel, title, values,
    } = this.props;
    const { getFieldDecorator } = this.props.form;
    const formItemLayout = {
      labelCol: { span: 5 },
      wrapperCol: { span: 17 },
    };
    const uploadProps = {
      action: api.uploadImg, // 上传地址
      multiple: false,
      name: 'file',
      headers: {
        Authorization: localStorage.getItem('token'),
      },
    };
    const imgProps = {
      onChange: this.uploadImg.bind(this, 'imgUrl'),
      accept: 'image/*',
      ...uploadProps,
    };
    const evidenceImgProps = {
      onChange: this.uploadImg.bind(this, 'evidenceImg'),
      accept: 'image/*',
      ...uploadProps,
    };
    const evidenceVideoProps = {
      onChange: this.uploadImg.bind(this, 'evidenceVideo'),
      onRemove: this.handleRemove.bind(this, 'evidenceVideo'),
      accept: 'video/*',
      ...uploadProps,
    };
    return (
      <Modal
        visible={visible}
        title={title}
        footer={this.footer()}
        onCancel={onCancel}
        bodyStyle={{ height: 400 }}
      >
        <Form onSubmit={this.handleSubmit}>
          <FormItem {...formItemLayout} label="证书编号" hasFeedback>
            {getFieldDecorator('certNumber', {
              initialValue: values ? values.certNumber : '',
              rules: [{ required: true, message: '请输入证书编号' }],
            })(<Input placeholder="请输入证书编号" maxLength={50} />)}
          </FormItem>
          <FormItem {...formItemLayout} label="角色名称" hasFeedback>
            {getFieldDecorator('roleName', {
              initialValue: values ? values.roleName : '',
              rules: [{ required: true, message: '请输入角色名称' }],
            })(<Input placeholder="请输入角色名称" maxLength={100} />)}
          </FormItem>
          <FormItem {...formItemLayout} label="照片" >
            {getFieldDecorator('imgUrl', {
              initialValue: { fileList: values && values.imgUrl ? [{ url: values.imgUrl }] : [] },
              rules: [{ required: true, message: '请上传照片', validator: this.checkFileUpload }],

            })(<Upload {...imgProps} fileList={this.state.imgList}>
              <Button>
                <Icon type="upload" /> 上传
              </Button>
            </Upload>)}
          </FormItem>
          <FormItem {...formItemLayout} label="动漫名称" hasFeedback>
            {getFieldDecorator('cartoonName', {
              initialValue: values ? values.cartoonName : '',
              rules: [
                { required: true, message: '请输入动漫名称' },
              ],
            })(<Input placeholder="请输入动漫名称" maxLength={100} />)}
          </FormItem>
          <FormItem {...formItemLayout} label="载体" hasFeedback>
            {getFieldDecorator('itemType', {
              initialValue: values ? values.itemType : '',
              rules: [
                { required: true, message: '请输入载体' },
              ],
            })(<Input placeholder="请输入载体" maxLength={25} />)}
          </FormItem>
          <FormItem {...formItemLayout} label="原作者" hasFeedback>
            {getFieldDecorator('author', {
              initialValue: values ? values.author : '',
              rules: [
                { required: true, message: '请输入原作者' },
              ],
            })(<Input placeholder="请输入原作者" maxLength={100} />)}
          </FormItem>
          <FormItem {...formItemLayout} label="制作公司" hasFeedback>
            {getFieldDecorator('company', {
              initialValue: values ? values.company : '',
              rules: [
                { required: true, message: '请输入制作公司' },
              ],
            })(<Input placeholder="请输入制作公司" maxLength={200} />)}
          </FormItem>
          <FormItem {...formItemLayout} label="证据图片">
            {getFieldDecorator('evidenceImg', {
              initialValue: { fileList: values && values.evidenceImg ? [{ url: values.evidenceImg }] : [] },
            })(<Upload {...evidenceImgProps} fileList={this.state.evidenceImgList} >
              <Button>
                <Icon type="upload" /> 上传
              </Button>
            </Upload>)}
          </FormItem>
          <FormItem {...formItemLayout} label="证据视频">
            {getFieldDecorator('evidenceVideo', {
              initialValue: { fileList: values && values.evidenceVideo ? [{ url: values.evidenceVideo }] : [] },
            })(<Upload {...evidenceVideoProps} fileList={this.state.evidenceVideoList}>
              <Button>
                <Icon type="upload" /> 上传
              </Button>
            </Upload>)}
          </FormItem>
          <FormItem {...formItemLayout} label="评级" hasFeedback>
            {getFieldDecorator('score', {
              initialValue: values ? values.score : '',
              rules: [
                { required: true, message: '请输入评级' },
              ],
            })(<Input placeholder="请输入评级" maxLength={50} />)}
          </FormItem>
          <FormItem {...formItemLayout} label="备注" hasFeedback>
            {getFieldDecorator('remark', {
              initialValue: values ? values.remark : '',
            })(<Input placeholder="请输入备注" maxLength={500} />)}
          </FormItem>
          <div hidden>
            {getFieldDecorator('evidenceVideoImgUrl', {
              initialValue: values ? values.evidenceVideoImgUrl : '',
            })}
          </div>
        </Form>
      </Modal>
    )
  }
}
