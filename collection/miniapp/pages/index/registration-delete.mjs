export function canDeleteRegistration(item, states, ready) {
  return ready && !!item && [0, 2].includes(item.status)
    && item.tradeStatus === 0 && item.getbackStatus === 0 && !states[item.id];
}
