/**
 * 农助手公益平台 — 作业类型与设备分类字典
 *
 * 定位：公益信息对接平台，帮助农户找到本地有设备能帮忙的服务者。
 * 本文件定义全平台统一的作业类型、设备大类、服务项目分类体系。
 *
 * 包含：
 *   CROPS              — 作物分类（含主产区信息）
 *   MACH_CATEGORIES    — 作业环节大类
 *   MACHINES           — 具体机械型号
 *   SERVICE_CATEGORIES — 一级作业分类（农户发布需求入口）
 *   SERVICE_ITEMS      — 二级服务项（40+ 细分）
 *   EQUIPMENT_TYPES    — 设备大类（服务者建档用）
 */

// 1. 作物分类 (Crops) - 注入地缘主产区 (mainRegions)
export const CROPS = [
  // 粮食作物 (粮油安全)
  { id: 'WHEAT_WINTER', name: '冬小麦', type: '粮食作物', emoji: '🌾', mainRegions: ['河南', '山东', '河北', '安徽'] },
  { id: 'WHEAT_SPRING', name: '春小麦', type: '粮食作物', emoji: '🌾', mainRegions: ['内蒙古', '黑龙江', '甘肃', '新疆'] },
  { id: 'RICE_EARLY', name: '早稻', type: '粮食作物', emoji: '🌿', mainRegions: ['湖南', '江西', '广东', '广西'] },
  { id: 'RICE_MID_LATE', name: '中晚稻', type: '粮食作物', emoji: '🌿', mainRegions: ['黑龙江', '湖南', '江西', '安徽'] },
  { id: 'CORN_YELLOW', name: '大田玉米', type: '粮食作物', emoji: '🌽', mainRegions: ['黑龙江', '吉林', '山东', '河南'] },
  { id: 'SOYBEAN', name: '大豆', type: '粮食作物', emoji: '🌱', mainRegions: ['黑龙江', '内蒙古', '安徽'] },
  { id: 'POTATO', name: '马铃薯', type: '粮食作物', emoji: '🥔', mainRegions: ['甘肃', '内蒙古', '贵州', '云南'] },
  
  // 经济作物 (中国特色特种品)
  { id: 'PEANUT', name: '花生', type: '经济作物', emoji: '🥜', mainRegions: ['山东', '河南'] },
  { id: 'COTTON', name: '新疆棉花', type: '经济作物', emoji: '☁️', mainRegions: ['新疆'] },
  { id: 'RAPESEED', name: '油菜', type: '经济作物', emoji: '🌼', mainRegions: ['四川', '湖南', '湖北'] },
  { id: 'SUGARCANE', name: '甘蔗', type: '经济作物', emoji: '🎋', mainRegions: ['广西', '广东', '云南'] },
  { id: 'SUGARBEET', name: '甜菜', type: '经济作物', emoji: '🧅', mainRegions: ['内蒙古', '新疆', '黑龙江'] },
  { id: 'GARLIC', name: '大蒜', type: '经济作物', emoji: '🧄', mainRegions: ['山东', '河南', '江苏'] },
  
  // 果蔬林茶 (丘陵经济)
  { id: 'TEA', name: '茶树', type: '果蔬林茶', emoji: '🍵', mainRegions: ['浙江', '安徽', '云南', '武夷山'] },
  { id: 'CITRUS', name: '柑橘/柚子', type: '果蔬林茶', emoji: '🍊', mainRegions: ['广西', '湖南', '四川', '江西'] },
  { id: 'APPLE', name: '苹果', type: '果蔬林茶', emoji: '🍎', mainRegions: ['陕西', '山东', '甘肃'] },
  { id: 'VEGETABLE_GREEN', name: '大棚绿叶菜', type: '果蔬林茶', emoji: '🥬', mainRegions: ['全国(市郊)'] },
  
  { id: 'OTHER_MISC', name: '其他杂粮特产', type: '其他', emoji: '🌱', mainRegions: ['全国'] }
]

// 2. 作业环节大类 (Macinery Categories)
export const MACH_CATEGORIES = [
  { id: 'TILLAGE', name: '耕整地', desc: '深松、旋耕、起垄', emoji: '🚜' },
  { id: 'PLANTING', name: '开沟与种植', desc: '播种、水田插秧', emoji: '🌱' },
  { id: 'MANAGEMENT', name: '田间保教与植保', desc: '飞防、喷淋、除草', emoji: '🚁' },
  { id: 'HARVEST', name: '核心收获', desc: '割脱、机采、打捆', emoji: '🌾' }
]

// 3. 具体机械型号 (Machinery Types) - 全域流转解锁版 (无死锁)
export const MACHINES = [
  // 耕整地
  { id: 'SUB_TILLER', categoryId: 'TILLAGE', name: '重型深松机', desc: '打破深耕犁底层', icon: 'subsoiler', isCrossRegional: true },
  { id: 'ROTARY_TILLER', categoryId: 'TILLAGE', name: '轮式旋耕机', desc: '广谱旱地碎土', icon: 'tiller', isCrossRegional: true },
  { id: 'MICRO_TILLER', categoryId: 'TILLAGE', name: '履带微耕机', desc: '复杂小地块神器', icon: 'tiller', isCrossRegional: true },
  
  // 种植
  { id: 'MULTI_SEEDER', categoryId: 'PLANTING', name: '多功能条播机', desc: '旱田精量播种', icon: 'seeder', isCrossRegional: true },
  { id: 'FLOAT_TRANSPLANTER', categoryId: 'PLANTING', name: '高速插秧机', desc: '水田超高速抛秧', icon: 'transplanter', isCrossRegional: true },
  
  // 田间管理
  { id: 'AGRI_DRONE_DJI', categoryId: 'MANAGEMENT', name: '农用植保无人机', desc: '抗灾飞防全地形空中喷洒', icon: 'drone', isCrossRegional: true }, // 极高机动性
  { id: 'ORCHARD_MOWER', categoryId: 'MANAGEMENT', name: '自走式除草机', desc: '低矮树冠行间清理', icon: 'sprayer', isCrossRegional: true },
  
  // 收获
  { id: 'COMBINE_WHEEL', categoryId: 'HARVEST', name: '轮式联合收割机', desc: '良好地况极速抢收主力', icon: 'combine', isCrossRegional: true }, 
  { id: 'COMBINE_CRAWLER', categoryId: 'HARVEST', name: '履带式收割机', desc: '烂泥涝灾水田全域救援克星', icon: 'combine', isCrossRegional: true }, // 抗险救灾全能
  { id: 'CORN_COB_HARVESTER', categoryId: 'HARVEST', name: '玉米茎穗兼收机', desc: '摘穗碎混全能', icon: 'combine', isCrossRegional: true },
  { id: 'COTTON_PICKER_HEAVY', categoryId: 'HARVEST', name: '重型采棉机', desc: '超重型集群化采棉', icon: 'cotton_picker', isCrossRegional: true }, // 完全解禁
  { id: 'BALER_SQUARE', categoryId: 'HARVEST', name: '方草捆打捆机', desc: '秸秆离田环保必备', icon: 'baler', isCrossRegional: true },
  { id: 'SUGARCANE_HARVESTER', categoryId: 'HARVEST', name: '甘蔗收获机', desc: '高杆作物克星', icon: 'baler', isCrossRegional: true } // 完全解禁
]

// 辅助方法：根据大类获取机械列表
export function getMachinesByCategory(categoryId) {
  return MACHINES.filter(m => m.categoryId === categoryId)
}

// ============================================================
// 以下为设计说明书 V1.0 三层分类体系
// 「作业场景 → 二级服务项 → 设备/主机/机具」
// 注意：与上方 MACH_CATEGORIES / MACHINES 并存，互不干扰
// ============================================================

/**
 * 4. 一级作业分类（农户发布需求入口）
 * 农户不选机器，先选「要干什么活」
 * 对应设计文档 5.1 章 — 10大类
 */
export const SERVICE_CATEGORIES = [
  { id: 1,  code: 'TILLAGE',       name: '耕地整地', emoji: '🚜', desc: '翻地、旋耕、深松、起垄、开沟、平地' },
  { id: 2,  code: 'PLANTING',      name: '播种插秧', emoji: '🌱', desc: '条播、穴播、精量播种、插秧、移栽、覆膜' },
  { id: 3,  code: 'SPRAYING',      name: '施肥打药', emoji: '💊', desc: '大田打药、果园打药、撒肥、施肥' },
  { id: 4,  code: 'IRRIGATION',    name: '灌溉排水', emoji: '💧', desc: '抽水灌溉、喷灌、滴灌、排水、排涝' },
  { id: 5,  code: 'HARVEST',       name: '收割收获', emoji: '🌾', desc: '水稻/小麦/玉米/花生/马铃薯/甘蔗收获' },
  { id: 6,  code: 'STRAW',         name: '秸秆处理', emoji: '🔥', desc: '粉碎还田、打捆、脱粒、烘干' },
  { id: 7,  code: 'ORCHARD',       name: '果园作业', emoji: '🍊', desc: '果园打药、开沟施肥、除草、园内运输' },
  { id: 8,  code: 'TRANSPORT',     name: '运输装卸', emoji: '🚛', desc: '地头短驳、粮食转运、农资装卸' },
  { id: 9,  code: 'GREENHOUSE',    name: '大棚/设施', emoji: '🏡', desc: '大棚微耕、设施灌溉、温室打药、卷帘' },
  { id: 10, code: 'OTHER',         name: '其他作业', emoji: '🔧', desc: '以上未涵盖的特殊需求' },
]

/**
 * 5. 二级服务项（农户发布需求时选的具体活儿）
 * 对应设计文档 5.2 章 — 40+ 细分服务项
 * categoryId 关联 SERVICE_CATEGORIES.id
 */
export const SERVICE_ITEMS = [
  // ── 耕地整地 ──
  { id: 101, categoryId: 1, code: 'TILL_PLOW',     name: '翻地',     desc: '铧犁深翻土地', matchEquip: ['PLOW'] },
  { id: 102, categoryId: 1, code: 'TILL_ROTARY',   name: '旋耕',     desc: '旋耕机碎土整地', matchEquip: ['ROTARY_TILLER'] },
  { id: 103, categoryId: 1, code: 'TILL_SUBSOIL',  name: '深松',     desc: '打破犁底层、改善土壤通透性', matchEquip: ['SUBSOILER'] },
  { id: 104, categoryId: 1, code: 'TILL_RIDGE',    name: '起垄',     desc: '起垄做畦', matchEquip: ['RIDGER'] },
  { id: 105, categoryId: 1, code: 'TILL_DITCH',    name: '开沟',     desc: '挖排水沟或施肥沟', matchEquip: ['RIDGER'] },
  { id: 106, categoryId: 1, code: 'TILL_LEVEL',    name: '平地',     desc: '激光平地或刮板平地', matchEquip: ['PADDY_TILLER'] },

  // ── 播种插秧 ──
  { id: 201, categoryId: 2, code: 'SEED_ROW',      name: '条播',     desc: '行距均匀条播（小麦/豆类）', matchEquip: ['WHEAT_SEEDER'] },
  { id: 202, categoryId: 2, code: 'SEED_HOLE',     name: '穴播',     desc: '按穴定量播种', matchEquip: ['CORN_SEEDER'] },
  { id: 203, categoryId: 2, code: 'SEED_PRECISION', name: '精量播种', desc: '单粒精播省种省工', matchEquip: ['CORN_SEEDER'] },
  { id: 204, categoryId: 2, code: 'TRANSPLANT',    name: '插秧',     desc: '水田机械插秧', matchEquip: ['RICE_TRANSPLANTER'] },
  { id: 205, categoryId: 2, code: 'SEEDLING',      name: '移栽',     desc: '蔬菜/烟草秧苗移栽', matchEquip: ['RICE_TRANSPLANTER'] },
  { id: 206, categoryId: 2, code: 'MULCH',         name: '覆膜',     desc: '地膜覆盖保墒保温', matchEquip: ['MULCH_LAYER'] },

  // ── 施肥打药 ──
  { id: 301, categoryId: 3, code: 'SPRAY_FIELD',   name: '大田打药', desc: '大面积农田植保喷药', matchEquip: ['DRONE_SPRAYER', 'BOOM_SPRAYER'] },
  { id: 302, categoryId: 3, code: 'SPRAY_ORCHARD', name: '果园打药', desc: '果树病虫害防治喷药', matchEquip: ['KNAPSACK_SPRAYER'] },
  { id: 303, categoryId: 3, code: 'FERT_SPREAD',   name: '撒肥',     desc: '机械撒施底肥/追肥', matchEquip: ['SPREADER'] },
  { id: 304, categoryId: 3, code: 'FERT_APPLY',    name: '施肥',     desc: '开沟施肥或深施', matchEquip: ['SPREADER'] },

  // ── 灌溉排水 ──
  { id: 401, categoryId: 4, code: 'PUMP_IRRIGATE', name: '抽水灌溉', desc: '水泵抽水灌溉农田', matchEquip: ['WATER_PUMP'] },
  { id: 402, categoryId: 4, code: 'SPRINKLER',     name: '喷灌',     desc: '卷盘喷灌机灌溉', matchEquip: ['WATER_PUMP'] },
  { id: 403, categoryId: 4, code: 'DRIP',          name: '滴灌',     desc: '滴灌设备安装/维护', matchEquip: ['WATER_PUMP'] },
  { id: 404, categoryId: 4, code: 'DRAIN',         name: '排水',     desc: '农田排水作业', matchEquip: ['WATER_PUMP'] },
  { id: 405, categoryId: 4, code: 'FLOOD_PUMP',    name: '排涝',     desc: '洪涝期紧急排涝', matchEquip: ['WATER_PUMP'] },

  // ── 收割收获 ──
  { id: 501, categoryId: 5, code: 'HARV_RICE',     name: '水稻收割', desc: '联合收割机收水稻', matchEquip: ['RICE_HARVESTER'] },
  { id: 502, categoryId: 5, code: 'HARV_WHEAT',    name: '小麦收割', desc: '联合收割机收小麦', matchEquip: ['WHEAT_HARVESTER', 'GRAIN_HARVESTER'] },
  { id: 503, categoryId: 5, code: 'HARV_CORN',     name: '玉米收获', desc: '玉米收获机摘穗脱粒', matchEquip: ['CORN_HARVESTER'] },
  { id: 504, categoryId: 5, code: 'HARV_PEANUT',   name: '花生收获', desc: '花生收获机摘果清选', matchEquip: ['PEANUT_HARVESTER'] },
  { id: 505, categoryId: 5, code: 'HARV_POTATO',   name: '马铃薯收获', desc: '马铃薯挖掘收获', matchEquip: ['VEG_HARVESTER'] },
  { id: 506, categoryId: 5, code: 'HARV_CANE',     name: '甘蔗收获', desc: '甘蔗收获机作业', matchEquip: ['SUGARCANE_HARVESTER'] },

  // ── 秸秆处理 ──
  { id: 601, categoryId: 6, code: 'STRAW_CHOP',    name: '粉碎还田', desc: '秸秆就地粉碎还田', matchEquip: ['BALER'] },
  { id: 602, categoryId: 6, code: 'STRAW_BALE',    name: '打捆',     desc: '秸秆打捆离田', matchEquip: ['BALER'] },
  { id: 603, categoryId: 6, code: 'THRESH',        name: '脱粒',     desc: '小规模谷物脱粒', matchEquip: ['THRESHER'] },
  { id: 604, categoryId: 6, code: 'DRY',           name: '烘干',     desc: '粮食烘干防霉', matchEquip: ['GRAIN_DRYER'] },

  // ── 果园作业 ──
  { id: 701, categoryId: 7, code: 'ORCH_SPRAY',    name: '果园打药', desc: '风送喷雾机果树植保', matchEquip: ['KNAPSACK_SPRAYER'] },
  { id: 702, categoryId: 7, code: 'ORCH_FERT',     name: '开沟施肥', desc: '果园开沟施肥一体机', matchEquip: ['SPREADER'] },
  { id: 703, categoryId: 7, code: 'ORCH_WEED',     name: '除草',     desc: '果园行间除草', matchEquip: ['MICRO_TILLER'] },
  { id: 704, categoryId: 7, code: 'ORCH_HAUL',     name: '园内运输', desc: '果品/农资短途运输', matchEquip: ['AGRI_TRUCK'] },

  // ── 运输装卸 ──
  { id: 801, categoryId: 8, code: 'TRANS_SHORT',   name: '地头短驳', desc: '田间到路边短途运输', matchEquip: ['AGRI_TRUCK'] },
  { id: 802, categoryId: 8, code: 'TRANS_GRAIN',   name: '粮食转运', desc: '粮食从产地到仓库/收购点', matchEquip: ['AGRI_TRUCK'] },
  { id: 803, categoryId: 8, code: 'TRANS_LOAD',    name: '农资装卸', desc: '化肥、种子等物资装卸', matchEquip: ['AGRI_TRUCK'] },

  // ── 大棚/设施 ──
  { id: 901, categoryId: 9, code: 'GH_TILL',       name: '大棚微耕', desc: '温室内小型旋耕', matchEquip: ['MICRO_TILLER'] },
  { id: 902, categoryId: 9, code: 'GH_IRRIGATE',   name: '设施灌溉', desc: '温室喷灌/滴灌维护', matchEquip: ['WATER_PUMP'] },
  { id: 903, categoryId: 9, code: 'GH_SPRAY',      name: '温室打药', desc: '大棚内植保喷雾', matchEquip: ['KNAPSACK_SPRAYER'] },
  { id: 904, categoryId: 9, code: 'GH_CURTAIN',    name: '卷帘',     desc: '温室卷帘设备安装/维修', matchEquip: [] },
]

/**
 * 6. 设备大类（农机主端建档用）
 * 对应设计文档 5.3 章 — 7大设备类别
 */
export const EQUIPMENT_TYPES = [
  { id: 'A', code: 'POWER',      name: 'A. 动力主机类',   items: ['轮式拖拉机', '履带拖拉机', '手扶拖拉机', '微耕机', '自走式动力平台'] },
  { id: 'B', code: 'IMPLEMENT',  name: 'B. 配套机具类',   items: ['铧式犁', '旋耕机', '深松机', '开沟机', '起垄机', '平地机', '条播机', '穴播机', '精量播种机', '覆膜机', '施肥机', '撒肥机', '中耕除草机', '秸秆粉碎还田机', '捡拾压捆机'] },
  { id: 'C', code: 'HARVESTER',  name: 'C. 收获机械类',   items: ['联合收割机（小麦/水稻）', '玉米收获机', '花生收获机', '马铃薯收获机', '甘蔗收获机'] },
  { id: 'D', code: 'PROTECTION', name: 'D. 植保施肥类',   items: ['植保无人机', '自走式喷杆喷雾机', '果园风送喷雾机', '撒肥机', '施肥机'] },
  { id: 'E', code: 'WATER',      name: 'E. 灌溉排涝类',   items: ['柴油水泵', '电动水泵', '卷盘喷灌机', '滴灌设备', '排涝泵'] },
  { id: 'F', code: 'ORCHARD',    name: 'F. 果园与设施类', items: ['果园开沟施肥机', '园艺运输车', '大棚微耕机', '大棚喷雾机', '卷帘机'] },
  { id: 'G', code: 'LOGISTICS',  name: 'G. 运输装卸类',   items: ['农用运输车', '拖车', '装载机'] },
]

// ── 辅助查询方法 ──

/** 根据一级作业分类获取二级服务项列表 */
export function getServiceItemsByCategory(categoryId) {
  return SERVICE_ITEMS.filter(item => item.categoryId === categoryId)
}

/** 根据 code 查找一级作业分类 */
export function findServiceCategory(code) {
  return SERVICE_CATEGORIES.find(c => c.code === code)
}

/** 根据 code 查找二级服务项 */
export function findServiceItem(code) {
  return SERVICE_ITEMS.find(item => item.code === code)
}
