import { DEMO_ASSETS } from '@/sheep/config/demo-content'

export const COLLECTION_CATEGORIES = [
  {
    id: 'preview-card',
    name: '评级卡砖',
    picUrl: DEMO_ASSETS.gradedCard,
  },
  {
    id: 'preview-signature',
    name: '签名卡砖',
    picUrl: DEMO_ASSETS.gradedCard,
  },
  {
    id: 'preview-badge',
    name: '评级吧唧',
    picUrl: DEMO_ASSETS.enamelBadge,
  },
  {
    id: 'preview-paper',
    name: '评级色纸',
    picUrl: DEMO_ASSETS.artBoard,
  },
  {
    id: 'preview-funko',
    name: '评级funko',
    picUrl: DEMO_ASSETS.figure,
  },
  {
    id: 'preview-folder',
    name: '评级文件夹',
    picUrl: DEMO_ASSETS.artBoard,
  },
  {
    id: 'preview-emblem',
    name: '评级徽章/纪念章',
    picUrl: DEMO_ASSETS.enamelBadge,
  },
  {
    id: 'preview-custom',
    name: '自定义',
    picUrl: DEMO_ASSETS.gradedCard,
  },
  {
    id: 'preview-heritage',
    name: '非遗评级',
    picUrl: DEMO_ASSETS.medal,
  },
]

export const COLLECTION_CATEGORY_OPTIONS = [
  { id: 'all', name: '全部' },
  ...COLLECTION_CATEGORIES.map(({ id, name }) => ({ id, name })),
]
