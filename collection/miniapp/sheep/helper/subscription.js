export const SUBSCRIPTION_ONBOARDING_KEY = 'ys_subscription_onboarding_v1';
export const SUBSCRIPTION_PREFERENCE_KEY = 'ys_subscription_preferences_v1';

export const subscriptionModules = [
  {
    key: 'collection',
    title: '藏品与审核',
    description: '登记、审核与入库状态',
    icon: '藏',
    keywords: ['藏品', '登记', '审核', '入库'],
    required: ['审核驳回或需补充资料', '登记或入库出现异常'],
    recommended: ['审核通过', '藏品入库完成'],
  },
  {
    key: 'trade',
    title: '交易与订单',
    description: '售出、订单和退款状态',
    icon: '交',
    keywords: ['订单', '交易', '售出', '退款', '取消'],
    required: ['商品已售出', '订单取消或退款结果'],
    recommended: ['商品上架或下架成功', '买家订单状态变化'],
  },
  {
    key: 'auction',
    title: '拍卖进度',
    description: '上架、成交与流拍结果',
    icon: '拍',
    keywords: ['拍卖', '竞拍', '出价', '流拍'],
    required: ['拍卖已上架', '拍卖成交或流拍'],
    recommended: ['拍卖即将结束', '竞拍价格发生变化'],
  },
  {
    key: 'getback',
    title: '取回与物流',
    description: '受理、发货和物流异常',
    icon: '取',
    keywords: ['取回', '物流', '发货', '签收'],
    required: ['取回已受理、取消或驳回', '物流已发货或出现异常'],
    recommended: ['包裹已签收', '物流节点更新'],
  },
  {
    key: 'wallet',
    title: '钱包与资金',
    description: '结算、入账和提现结果',
    icon: '资',
    keywords: ['钱包', '充值', '提现', '到账', '结算', '打款', '收款'],
    required: ['售出款进入钱包', '提现或打款成功、失败'],
    recommended: ['结算单已生成', '待收款提醒'],
  },
  {
    key: 'system',
    title: '系统与安全',
    description: '账户风险和服务异常',
    icon: '系',
    keywords: ['系统', '服务', '维护', '安全', '异常'],
    required: ['账户安全风险', '影响业务的系统异常'],
    recommended: ['服务维护公告', '功能更新提醒'],
  },
];

export function getSubscriptionPreferences() {
  const saved = uni.getStorageSync(SUBSCRIPTION_PREFERENCE_KEY) || {};
  return subscriptionModules.reduce((result, item) => {
    result[item.key] = saved[item.key] !== false;
    return result;
  }, {});
}

export function saveSubscriptionPreference(moduleKey, enabled) {
  const preferences = getSubscriptionPreferences();
  preferences[moduleKey] = Boolean(enabled);
  uni.setStorageSync(SUBSCRIPTION_PREFERENCE_KEY, preferences);
  return preferences;
}

export function matchTemplatesToModules(templates = []) {
  const usedIds = new Set();
  return subscriptionModules.reduce((result, module) => {
    result[module.key] = templates.filter((template) => {
      if (!template?.id || usedIds.has(template.id)) return false;
      const matched = module.keywords.some((keyword) => template.title?.includes(keyword));
      if (matched) usedIds.add(template.id);
      return matched;
    });
    return result;
  }, {});
}

export function maybeShowSubscriptionOnboarding() {
  // #ifndef MP-WEIXIN
  return;
  // #endif

  if (uni.getStorageSync(SUBSCRIPTION_ONBOARDING_KEY)) return;
  uni.setStorageSync(SUBSCRIPTION_ONBOARDING_KEY, Date.now());

  setTimeout(() => {
    uni.showModal({
      title: '开启重要服务通知',
      content:
        '及时接收商品售出、取回进度、拍卖状态、物流发货和资金到账提醒。不会发送营销信息，可随时在设置中管理。',
      confirmText: '管理订阅',
      cancelText: '暂不设置',
      success: ({ confirm }) => {
        if (confirm) {
          uni.navigateTo({ url: '/pages/public/subscription' });
        }
      },
    });
  }, 700);
}
