<template>
  <view class="receipt">
    <view class="heading">{{ detail?.status === 11 ? '提现已到账' : '提现申请已提交' }}</view>
    <view class="money">¥{{ fen2yuan(amount) }}</view>
    <view class="notice">{{ hint }}</view>
    <button v-if="canConfirm" class="primary" :loading="busy" :disabled="busy" hover-class="pressed" @tap="confirmReceipt">继续收款</button>
    <button v-else-if="detail?.status !== 11" class="primary" :loading="busy" :disabled="busy" hover-class="pressed" @tap="refreshAndConfirm">刷新收款状态</button>
    <button class="secondary" :disabled="busy" hover-class="pressed" @tap="sheep.$router.go('/pages/commission/wallet', { type: 2 })">查看提现记录</button>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import sheep from '@/sheep';
import BrokerageApi from '@/sheep/api/trade/brokerage';
import PayTransferApi from '@/sheep/api/pay/transfer';
import { fen2yuan } from '@/sheep/hooks/useGoods';
const props = defineProps({ id: { required: true }, amount: { type: Number, required: true } });
const detail = ref(null);
const busy = ref(false);
const error = ref('');
let disposed = false;
let initialized = false;
const canConfirm = computed(() => detail.value?.status === 10 && detail.value?.type === 5 && !!detail.value?.transferChannelMchId && !!detail.value?.transferChannelPackageInfo);
const hint = computed(() => {
  if (error.value) return error.value;
  if (detail.value?.status === 11) return '微信已返回转账成功，请查看微信零钱账单。';
  if ([20, 21].includes(detail.value?.status)) return '本笔提现未成功，请查看提现记录中的处理结果。';
  if (busy.value && canConfirm.value) return '请在微信弹出的收款页面确认，正在等待收款结果。';
  if (canConfirm.value) return '请在微信收款页面确认；如已取消或未弹出，可点击继续收款，无需重新提现。';
  if (busy.value) return '正在获取微信收款信息，请稍候，不要重复提现。';
  return '正在核对转账状态，可点击刷新。请勿重复提交提现。';
});
async function readDetail() {
  const res = await BrokerageApi.getBrokerageWithdraw(props.id);
  if (res?.code !== 0 || !res.data) throw new Error('状态查询失败，请重试或查看提现记录。');
  detail.value = res.data;
}
async function refresh() {
  if (busy.value || disposed) return false;
  busy.value = true; error.value = '';
  try {
    if (detail.value?.payTransferId) await PayTransferApi.syncTransfer(detail.value.payTransferId);
    await readDetail();
    return true;
  } catch { error.value = '状态暂时未更新，请稍后刷新，不要重复提现。'; }
  finally { busy.value = false; }
}
async function refreshAndConfirm() {
  // 仅新提现成功进入本组件或用户主动刷新时衔接收款；取消后不循环弹窗。
  const ready = await refresh();
  if (ready && !disposed) confirmReceipt();
}
async function initializeReceipt() {
  if (initialized || disposed) return;
  initialized = true;
  await refreshAndConfirm();
}
function confirmReceipt() {
  if (disposed || busy.value || !canConfirm.value) return;
  const provider = sheep.$platform.useProvider();
  if (!provider?.requestMerchantTransfer) { error.value = '请在微信中打开小程序完成收款。'; return; }
  // #ifdef MP-WEIXIN
  if (!wx.canIUse('requestMerchantTransfer')) { error.value = '请升级微信后确认收款，也可稍后从提现记录继续。'; return; }
  // #endif
  busy.value = true; error.value = '';
  const finished = async () => {
    try {
      await PayTransferApi.syncTransfer(detail.value.payTransferId);
      await readDetail();
      if (detail.value.status !== 11) error.value = '收款状态尚未确认，请刷新查看；若刚才取消，可再次点击确认收款。';
    } catch { error.value = '收款结果查询暂时失败，请刷新查看，不要重复提现。'; }
    finally { busy.value = false; }
  };
  try {
    provider.requestMerchantTransfer(detail.value.transferChannelMchId, detail.value.transferChannelPackageInfo,
      finished, () => { error.value = '收款未完成，可再次点击确认收款，或稍后从提现记录继续。'; busy.value = false; });
  } catch { error.value = '暂时无法打开微信收款页面，请重试。'; busy.value = false; }
}
onMounted(initializeReceipt);
onBeforeUnmount(() => { disposed = true; });
</script>

<style scoped lang="scss">
.receipt { margin: 24rpx; padding: 40rpx 32rpx; border-radius: 24rpx; background: var(--ys-surface, #fff); color: var(--ys-text, #18181b); text-align: center; padding-bottom: calc(40rpx + env(safe-area-inset-bottom)); }
.heading { font-size: 36rpx; font-weight: 700; }
.money { font-size: 56rpx; font-weight: 700; margin: 24rpx 0; overflow-wrap: anywhere; }
.notice { font-size: 28rpx; line-height: 1.6; margin-bottom: 32rpx; }
.primary, .secondary { min-height: 48px; line-height: 1.5; padding: 24rpx; font-size: 32rpx; border-radius: 16rpx; margin-top: 20rpx; }
.primary { background: var(--ys-text, #18181b); color: var(--ys-surface, #fff); font-weight: 700; }
.secondary { background: var(--ys-surface, #fff); color: var(--ys-text, #18181b); }
.pressed, button[disabled] { opacity: .65; }
</style>
