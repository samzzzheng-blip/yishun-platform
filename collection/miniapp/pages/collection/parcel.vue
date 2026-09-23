<template>
  <s-layout title="藏品交付登记" navbar="normal">
    <view class="parcel-page">
      <view v-if="loading" class="hint">正在加载寄送信息…</view>
      <view v-else-if="error" class="section">
        <view>{{ error }}</view
        ><button @tap="load">重新加载</button>
      </view>
      <view v-else-if="!config.enabled" class="hint">寄送登记暂未开放，请稍后再试。</view>
      <template v-else>
        <view class="section">
          <view class="heading">公司收件地址</view>
          <view class="hint">线下接收时间：{{ config.receivingHours || '周一至周五 9:00–18:00' }}</view>
          <view class="address">{{
            config.receivingAddress || '收件地址待工作人员配置，请确认后寄出。'
          }}</view>
          <button v-if="config.receivingAddress" size="mini" @tap="copy(config.receivingAddress)"
            >复制收件地址</button
          >
          <view class="hint"
            >可选择邮寄或线下送达，每次交付可关联多件藏品。尚未交付的藏品可稍后登记。</view
          >
        </view>
        <view class="section member-reminder">
          <view class="heading">交付前请留意</view>
          <view class="member-number">您的会员号：{{ memberId || (memberLoading ? '正在获取…' : '暂未获取') }}</view>
          <view>邮寄时，请在发件人姓名后注明会员号，保留真实姓名和联系电话，方便我们核对归属。</view>
          <view class="member-note-important">重要：请随物品放一张纸条，仅写会员号，方便我们核对归属。</view>
          <view class="member-actions">
            <button :disabled="!memberId" @tap="copy(memberId)">复制会员号</button>
            <button :disabled="!memberId" @tap="copyMemberNote">复制纸条内容</button>
            <button v-if="!memberId && !memberLoading" @tap="loadMember">重新获取会员号</button>
          </view>
        </view>
        <template v-if="editing">
          <view class="section">
            <view class="heading">{{ form.id ? '修改交付登记' : '填写交付信息' }}</view>
            <view class="label">交付方式（必选）</view>
            <radio-group class="delivery-options" @change="form.deliveryMethod = $event.detail.value">
              <label><radio value="courier" :checked="form.deliveryMethod === 'courier'" />邮寄</label>
              <label v-if="config.offlineEnabled"><radio value="offline" :checked="form.deliveryMethod === 'offline'" />线下送达</label>
            </radio-group>
            <view v-if="form.deliveryMethod === 'offline'" class="hint">无需填写单号。请在接收时间内送至上方公司地址，到店出示登记编号，工作人员收货核对后审核入库。</view>
            <template v-else>
            <view class="label">快递公司</view>
            <picker :range="carriers" :value="carrierIndex" @change="chooseCarrier">
              <view class="picker">{{ form.carrier || '请选择快递公司' }}</view>
            </picker>
            <input
              v-if="customCarrier"
              v-model="form.carrier"
              class="input"
              maxlength="60"
              placeholder="填写其他快递公司名称"
            />
            <view class="label">快递单号</view>
            <input
              v-model="form.trackingNo"
              class="input"
              maxlength="64"
              placeholder="输入单号，支持字母和数字"
            />
            <button size="mini" @tap="scan">扫描快递条码</button>
            </template>
            <view class="label">包裹备注（选填）</view>
            <textarea
              v-model="form.remark"
              class="input note"
              maxlength="500"
              placeholder="填写本包裹需要说明的情况"
            />
          </view>
          <view class="section">
            <view class="heading">本次交付 {{ form.collectionIds.length }} 件</view>
            <view class="hint">仅选择本次实际邮寄或送达的藏品；已关联其他登记的藏品不会重复显示。</view>
            <view v-if="!available.length" class="hint">暂无可寄送藏品，请先上传待审核藏品。</view>
            <checkbox-group @change="selectItems">
              <label v-for="item in available" :key="item.id" class="item">
                <checkbox
                  :value="String(item.id)"
                  :checked="form.collectionIds.includes(item.id)"
                  color="#7954d8"
                />
                <image
                  v-if="item.pic_url"
                  :src="sheep.$url.cdn(item.pic_url)"
                  mode="aspectFit"
                  class="photo"
                />
                <view class="item-text"
                  >{{ item.name }}<view class="hint">编号 {{ item.id }} · 1件</view></view
                >
              </label>
            </checkbox-group>
            <view
              v-for="item in available.filter((i) => form.collectionIds.includes(i.id))"
              :key="'note-' + item.id"
            >
              <view class="label">{{ item.name }} · 单件备注（选填）</view>
              <input
                v-model="form.itemNotes[item.id]"
                class="input"
                maxlength="500"
                placeholder="此件藏品需要单独说明的情况"
              />
            </view>
            <view v-if="formError" class="error">{{ formError }}</view>
            <button
              class="primary"
              :loading="saving"
              :disabled="saving || !form.collectionIds.length"
              @tap="save"
              >保存交付信息</button
            >
            <button :disabled="saving" @tap="editing = false">取消编辑</button>
          </view>
        </template>
        <template v-else-if="detail">
          <view class="section">
            <button size="mini" @tap="detail = null">返回包裹列表</button>
            <view class="heading">登记 {{ detail.id }} · {{ statusName(detail.status) }}</view>
            <view class="address">{{ detail.delivery_method === 'offline' ? '线下送达 · 无需单号' : detail.carrier + ' ' + detail.tracking_no }}</view>
            <view v-if="detail.remark" class="hint">包裹备注：{{ detail.remark }}</view>
            <view class="member-note-important">重要：请随物品附上纸条，仅写会员号{{ memberId ? `：${memberId}` : '' }}。</view>
            <button v-if="detail.status === 0" @tap="edit(detail)">修改交付信息或藏品</button>
            <button v-if="detail.status === 0" :disabled="saving" @tap="cancelParcel">撤销寄送登记</button>
            <button v-if="detail.status === 3" @tap="edit(detail)">重新登记此包裹</button>
            <view v-if="detail.status === 1 || detail.status === 2" class="hint">公司已收货，信息已锁定。如需更正，请联系工作人员。</view>
          </view>
          <view class="section">
            <view class="heading">包裹内藏品 · {{ detail.items.length }} 件</view>
            <view v-for="item in detail.items" :key="item.collection_id" class="item">
              <image
                v-if="item.pic_url"
                :src="sheep.$url.cdn(item.pic_url)"
                mode="aspectFit"
                class="photo"
              />
              <view class="item-text">
                <view>{{ item.name }} · {{ item.collection_id }}</view>
                <view class="hint">{{
                  item.audit_status === 1
                    ? '已审核入库'
                    : item.audit_status === 2
                      ? '审核未通过'
                      : item.status === 1
                        ? '已核对，待审核'
                        : item.status === 2
                          ? '异常待处理'
                          : '待核对'
                }}</view>
                <view v-if="item.note">{{ item.note }}</view>
                <image
                  v-if="item.evidence"
                  :src="sheep.$url.cdn(item.evidence)"
                  class="photo"
                  mode="aspectFit"
                  @tap="preview(item.evidence)"
                />
              </view>
            </view>
          </view>
          <view class="section">
            <view class="heading">处理记录</view>
            <view v-for="(event, i) in detail.events" :key="i" class="event">
              <view>{{ event.detail }}</view
              ><view class="hint">{{ event.create_time }}</view>
            </view>
          </view>
        </template>
        <template v-else>
          <button class="primary" @tap="edit()">登记邮寄 / 线下送达</button>
          <view class="hint">登记后请邮寄或线下送达；工作人员核对实物并审核通过后才会入库。</view>
          <view v-if="!parcels.length" class="section"
            >还没有交付登记。上传藏品后，在这里选择邮寄或线下送达。</view
          >
          <button v-for="p in parcels" :key="p.id" class="section parcel-link" @tap="open(p.id)">
            <view class="heading">包裹 {{ p.id }} · {{ statusName(p.status) }}</view>
            <view class="address">{{ p.delivery_method === 'offline' ? '线下送达 · 无需单号' : p.carrier + ' ' + p.tracking_no }}</view>
            <view class="hint"
              >预计 {{ p.expected_count }} 件 · 已核对收到 {{ p.received_count }} 件 ·
              查看详情</view
            >
          </button>
          <view class="pager"
            ><button size="mini" :disabled="page <= 1" @tap="changePage(-1)">上一页</button
            ><text>第 {{ page }} 页</text
            ><button size="mini" :disabled="parcels.length < 20" @tap="changePage(1)"
              >下一页</button
            ></view
          >
        </template>
      </template>
    </view>
  </s-layout>
</template>
<script setup>
  import { ref, reactive } from 'vue';
  import { onShow, onLoad } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import api from '@/sheep/api/collection/parcel';
  import UserApi from '@/sheep/api/member/user';
  import { memberNumber, memberNote } from './member-note.mjs';
  const memberId = ref(''), memberLoading = ref(false);
  async function loadMember() {
    if (memberLoading.value) return;
    memberId.value = '';
    memberLoading.value = true;
    try {
      const response = await UserApi.getUserInfo();
      if (response?.code === 0) memberId.value = memberNumber(response.data?.id);
    } catch { /* 保留地址展示，允许单独重试获取会员号 */ }
    finally { memberLoading.value = false; }
  }
  function copyMemberNote() {
    const text = memberNote(memberId.value);
    if (text) copy(text);
  }
  onShow(loadMember);
  const config = ref({ enabled: false, receivingAddress: '' });
  const parcels = ref([]),
    detail = ref(null),
    available = ref([]);
  const loading = ref(false),
    saving = ref(false),
    editing = ref(false),
    error = ref(''),
    formError = ref('');
  const page = ref(1),
    carrierIndex = ref(0),
    customCarrier = ref(false);
  onLoad((options) => {
    if (options.id) detail.value = { id: Number(options.id) };
  });
  const carriers = [
    '顺丰速运',
    '中通快递',
    '圆通速递',
    '申通快递',
    '韵达快递',
    '京东物流',
    '邮政EMS',
    '中国邮政',
    '极兔速递',
    '德邦快递',
    '其他',
  ];
  const form = reactive({
    id: null,
    version: null,
    deliveryMethod: 'courier',
    carrier: '',
    trackingNo: '',
    remark: '',
    collectionIds: [],
    itemNotes: {},
  });
  const statusName = (s) => ['已登记，待收货', '已收货，核对中', '已全部入库', '已撤销登记'][s] || '待处理';
  function cancelParcel() {
    if (saving.value) return;
    uni.showModal({
      title: '撤销寄送登记',
      content: '仅适用于尚未交付的藏品。撤销后可重新选择邮寄或线下送达，历史记录保留；实际快递不会因此取消。',
      confirmText: '确认撤销',
      success: async result => {
        if (!result.confirm) return;
        saving.value = true;
        try { unwrap(await api.cancel(detail.value.id)); await open(detail.value.id); }
        catch(e) { error.value = e.message || '撤销失败，请重试'; }
        finally { saving.value = false; }
      }
    });
  }
  const unwrap = (r) => {
    if (!r || r.code !== 0) throw new Error(r?.msg || '请求失败，请重试');
    return r.data;
  };
  async function load() {
    if (editing.value) return;
    loading.value = true;
    error.value = '';
    try {
      config.value = unwrap(await api.config());
      if (config.value.enabled) {
        parcels.value = unwrap(await api.list(page.value));
        if (detail.value) detail.value = unwrap(await api.detail(detail.value.id));
      }
    } catch (e) {
      error.value = e.message || '加载失败，请重试';
    } finally {
      loading.value = false;
    }
  }
  onShow(load);
  async function open(id) {
    loading.value = true;
    error.value = '';
    try {
      detail.value = unwrap(await api.detail(id));
    } catch (e) {
      error.value = e.message;
    } finally {
      loading.value = false;
    }
  }
  async function edit(p) {
    loading.value = true;
    error.value = '';
    try {
      available.value = unwrap(await api.available(p?.id));
      Object.assign(form, {
        id: p?.id || null,
        version: p?.version ?? null,
        deliveryMethod: p?.delivery_method || 'courier',
        carrier: p?.carrier || '',
        trackingNo: p?.tracking_no || '',
        remark: p?.remark || '',
        collectionIds: p ? p.items.map((i) => i.collection_id) : [],
        itemNotes: p ? Object.fromEntries(p.items.map((i) => [i.collection_id, i.note || ''])) : {},
      });
      customCarrier.value = !!form.carrier && !carriers.includes(form.carrier);
      carrierIndex.value = Math.max(0, carriers.indexOf(form.carrier));
      formError.value = '';
      editing.value = true;
    } catch (e) {
      error.value = e.message;
    } finally {
      loading.value = false;
    }
  }
  function chooseCarrier(e) {
    carrierIndex.value = Number(e.detail.value);
    customCarrier.value = carriers[carrierIndex.value] === '其他';
    form.carrier = customCarrier.value ? '' : carriers[carrierIndex.value];
  }
  function selectItems(e) {
    form.collectionIds = e.detail.value.map(Number);
  }
  function copy(text) {
    uni.setClipboardData({ data: text });
  }
  function preview(url) {
    uni.previewImage({ urls: [sheep.$url.cdn(url)] });
  }
  function scan() {
    uni.scanCode({
      scanType: ['barCode'],
      success: (r) => {
        form.trackingNo = r.result;
      },
      fail: () => {
        sheep.$helper.toast('未识别到条码，可以手动输入单号');
      },
    });
  }
  async function save() {
    if (saving.value) return;
    formError.value = '';
    if (
      (form.deliveryMethod !== 'offline' && (!form.carrier.trim() ||
      !/^[A-Za-z0-9-]{6,64}$/.test(form.trackingNo.trim()))) ||
      !form.collectionIds.length
    ) {
      formError.value = form.deliveryMethod === 'offline' ? '请勾选本次线下送达的藏品。' : '请选择快递公司、填写有效单号，并勾选本次邮寄的藏品。';
      return;
    }
    saving.value = true;
    try {
      const id = unwrap(
        await api.save({
          ...form,
          carrier: form.deliveryMethod === 'offline' ? '' : form.carrier.trim(),
          trackingNo: form.deliveryMethod === 'offline' ? '' : form.trackingNo.trim(),
        }),
      );
      editing.value = false;
      await open(id);
    } catch (e) {
      formError.value = e.message || '保存失败，请重试';
    } finally {
      saving.value = false;
    }
  }
  function changePage(delta) {
    page.value += delta;
    load();
  }
</script>
<style scoped lang="scss">
  .member-reminder { color: var(--ys-text); font-size: 28rpx; line-height: 1.7; }
  .member-reminder > view + view { margin-top: 16rpx; }
  .member-note-important { color: #a42b32; font-size: 30rpx; font-weight: 600; line-height: 1.7; }
  .member-number { font-size: 34rpx; font-weight: 600; word-break: break-all; }
  .member-actions { display: flex; flex-wrap: wrap; gap: 16rpx; }
  .member-actions button { flex: 1; min-width: 230rpx; min-height: 96rpx; margin: 0; padding: 16rpx; font-size: 28rpx; line-height: 1.6; }
  .parcel-page {
    padding: 24rpx;
    color: var(--ys-text, #242233);
    background: var(--ys-page-bg, #f6f5fa);
    min-height: 100vh;
  }
  .section {
    padding: 28rpx;
    margin-bottom: 24rpx;
    background: var(--ys-surface, #fff);
    border-radius: 20rpx;
  }
  .heading {
    font-size: 32rpx;
    font-weight: 600;
    margin: 12rpx 0 20rpx;
    line-height: 1.5;
  }
  .hint {
    color: var(--ys-text-secondary, #625e70);
    font-size: 26rpx;
    line-height: 1.7;
    margin: 14rpx 0;
  }
  .address,
  .item-text,
  .event {
    overflow-wrap: anywhere;
    word-break: break-all;
    line-height: 1.7;
    font-size: 28rpx;
  }
  .label {
    margin: 26rpx 0 12rpx;
    font-size: 28rpx;
  }
  .delivery-options {
    display: flex;
    flex-wrap: wrap;
    gap: 20rpx;
    label { display: flex; align-items: center; min-height: 96rpx; padding: 0 16rpx; font-size: 28rpx; }
  }
  .input,
  .picker {
    border: 1rpx solid var(--ys-border, #d4d0df);
    border-radius: 12rpx;
    padding: 22rpx;
    font-size: 28rpx;
    box-sizing: border-box;
  }
  .input {
    height: 88rpx;
  }
  .note {
    height: 170rpx;
    width: 100%;
  }
  .item {
    display: flex;
    align-items: center;
    gap: 18rpx;
    padding: 20rpx 0;
    border-bottom: 1rpx solid var(--ys-border, #e4e0eb);
  }
  .photo {
    width: 100rpx;
    height: 110rpx;
    flex-shrink: 0;
  }
  .item-text {
    flex: 1;
    min-width: 0;
  }
  button {
    margin-top: 20rpx;
    font-size: 28rpx;
    min-height: 80rpx;
    line-height: 2.7;
  }
  .primary {
    background: var(--ys-brand-color, #7954d8);
    color: #fff;
  }
  button[disabled] {
    opacity: 0.55;
  }
  .parcel-link {
    text-align: left;
    width: 100%;
    line-height: 1.5;
  }
  .error {
    color: #a42b32;
    line-height: 1.6;
    margin-top: 20rpx;
  }
  .event {
    padding: 16rpx 0;
    border-bottom: 1rpx solid var(--ys-border, #e4e0eb);
  }
  .pager {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 40rpx;
  }
</style>
