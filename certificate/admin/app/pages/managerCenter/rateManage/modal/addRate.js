
import React, { Component } from 'react'
import { Button, Form, Input, message, Upload, Icon, Modal, DatePicker } from 'antd'
import { postData } from '@http';
import moment from 'moment';

const FormItem = Form.Item;
import api from '@service/api';
import TextArea from 'antd/es/input/TextArea';

@Form.create({})

export default class Index extends Component {
  constructor(props) {
    super(props)
    let imgName;
    if (this.props.values) {
      imgName = this.props.values.imgUrl ? this.props.values.imgUrl.substring(this.props.values.imgUrl.lastIndexOf('\/') + 1) : null;
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
      this.setState({ loading: true }, () => {
        if (this.props.type === 'edit') {
          // 更新
          const params = {
            ...values,
            id: this.props.values.id,
          }

          postData('updateRate', params).then((data) => {
            message.success('更新成功');
            this.props.handleOk && this.props.handleOk();
            this.setState({ loading: false });
          }).catch((err) => {
            message.warning(err.message);
            this.setState({ loading: false });
          });
        } else {
          // 新增
          postData('addRate', values).then((data) => {
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
      }
      return file;
    });
    if (type === 'imgUrl') {
      this.setState({ imgList: fileList });
    }
  };

  choosePublishTime = (value) => {
    this.props.form.setFieldsValue({
      publishTime: `${moment(value, 'YYYY-MM-DD HH:mm:ss').valueOf()}`,
    });
  };

  handleRemove = (type, file) => {

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
    return (
      <Modal
        visible={visible}
        title={title}
        footer={this.footer()}
        onCancel={onCancel}
        bodyStyle={{ height: 400 }}
      >
        <Form onSubmit={this.handleSubmit}>
          <FormItem {...formItemLayout} label="编号" hasFeedback>
            {getFieldDecorator('certNumber', {
              initialValue: values ? values.certNumber : '',
              rules: [{ required: true, message: '请输入编号' }],
            })(<Input placeholder="请输入证书编号" maxLength={50} />)}
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
          <FormItem {...formItemLayout} label="名称" hasFeedback>
            {getFieldDecorator('rateName', {
              initialValue: values ? values.rateName : '',
              rules: [{ required: true, message: '请输入名称' }],
            })(<Input placeholder="请输入名称" maxLength={100} />)}
          </FormItem>
          <FormItem {...formItemLayout} label="表面" hasFeedback>
            {getFieldDecorator('surface', {
              initialValue: values ? values.surface : '',
              rules: [
                { required: true, message: '请输入表面' },
              ],
            })(<Input placeholder="请输入表面" maxLength={100} />)}
          </FormItem>
          <FormItem {...formItemLayout} label="居中" hasFeedback>
            {getFieldDecorator('center', {
              initialValue: values ? values.center : '',
              rules: [
                { required: true, message: '请输入居中' },
              ],
            })(<Input placeholder="请输入居中" maxLength={100} />)}
          </FormItem>
          <FormItem {...formItemLayout} label="边缘" hasFeedback>
            {getFieldDecorator('edge', {
              initialValue: values ? values.edge : '',
              rules: [
                { required: true, message: '请输入边缘' },
              ],
            })(<Input placeholder="请输入边缘" maxLength={100} />)}
          </FormItem>
          <FormItem {...formItemLayout} label="角落" hasFeedback>
            {getFieldDecorator('corner', {
              initialValue: values ? values.corner : '',
              rules: [
                { required: true, message: '请输入角落' },
              ],
            })(<Input placeholder="请输入角落" maxLength={100} />)}
          </FormItem>
          <FormItem {...formItemLayout} label="总评分" hasFeedback>
            {getFieldDecorator('score', {
              initialValue: values ? values.score : '',
              rules: [
                { required: true, message: '请输入总评分' },
              ],
            })(<Input placeholder="请输入总评分" maxLength={50} />)}
          </FormItem>
          <FormItem {...formItemLayout} label="备注" hasFeedback>
            {getFieldDecorator('remark', {
              initialValue: values ? values.remark : '',
            })(<TextArea placeholder="请输入备注" maxLength={500} />)}
          </FormItem>
        </Form>
      </Modal>
    )
  }
}
