export const DEMO_ASSETS = {
  banner: '/static/demo/home-collection-banner.jpg',
  gradedCard: '/static/demo/graded-card.jpg',
  enamelBadge: '/static/demo/enamel-badge.jpg',
  artBoard: '/static/demo/art-board.jpg',
  medal: '/static/demo/commemorative-medal.jpg',
  figure: '/static/demo/designer-figure.jpg',
  empty: '/static/demo/empty-collection.jpg',
}

export const DEMO_COLLECTIONS = [
  { name: '星轨典藏卡', cover: DEMO_ASSETS.gradedCard, price: 32900 },
  { name: '天穹珐琅徽章', cover: DEMO_ASSETS.enamelBadge, price: 18800 },
  { name: '远山限定色纸', cover: DEMO_ASSETS.artBoard, price: 26000 },
  { name: '月面纪念章', cover: DEMO_ASSETS.medal, price: 21900 },
  { name: '微光宇航员摆件', cover: DEMO_ASSETS.figure, price: 39900 },
]

export const DEMO_GOODS = Array.from({ length: 8 }, (_, index) => {
  const collection = DEMO_COLLECTIONS[index % DEMO_COLLECTIONS.length]
  return {
    id: `preview-good-${index + 1}`,
    name: collection.name,
    picUrl: [collection.cover],
    price: collection.price + index * 1200,
    status: 3,
    preview: true,
  }
})
