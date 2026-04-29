-- =============================================
-- V11: 农助手 V3 完整重构 — 片区信息对接平台
-- 策略：新建 v3_ 前缀表，旧表保留兼容
-- 农机分类依据：国家补贴目录 NY/T 1640 标准
-- =============================================

-- -----------------------------------------------
-- 1. 农机大类字典（v3_machine_category）
--    依据：农业农村部 2024-2026 补贴目录 25大类
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS v3_machine_category (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    code        VARCHAR(30)  NOT NULL UNIQUE COMMENT '大类编码',
    name        VARCHAR(60)  NOT NULL COMMENT '大类名称',
    icon        VARCHAR(10)  NULL COMMENT 'emoji图标',
    -- 跨区特征：CROSS_PROVINCE=跨省, CROSS_COUNTY=跨县, LOCAL=本地
    range_type  VARCHAR(20)  NOT NULL DEFAULT 'LOCAL' COMMENT '常见作业范围类型',
    sort_no     INT          NOT NULL DEFAULT 0,
    enabled     TINYINT      NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='农机大类字典';

-- -----------------------------------------------
-- 2. 农机小类字典（v3_machine_type）
--    记录每种机型可完成哪些作业、适用哪些作物
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS v3_machine_type (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    category_id     INT          NOT NULL COMMENT '所属大类ID',
    code            VARCHAR(50)  NOT NULL UNIQUE COMMENT '机型编码',
    name            VARCHAR(80)  NOT NULL COMMENT '机型名称',
    alias           VARCHAR(100) NULL COMMENT '农民口头叫法，逗号分隔',
    -- 作业范围：多数农机是本地作业，联合收割机等是跨区
    is_cross_region TINYINT      NOT NULL DEFAULT 0 COMMENT '是否常见跨区（省）作业',
    is_seasonal     TINYINT      NOT NULL DEFAULT 1 COMMENT '是否季节性作业',
    -- 适用季节：SPRING春/SUMMER夏/AUTUMN秋/WINTER冬/ALL全年
    work_seasons    VARCHAR(50)  NULL COMMENT '主要作业季节，逗号分隔',
    -- 适用作物编码，逗号分隔，*=通用
    suitable_crops  VARCHAR(200) NULL COMMENT '适用作物编码列表，*=多种',
    -- 适用地形
    terrain_type    VARCHAR(50)  NULL COMMENT '适用地形：PLAIN平原/HILL丘陵/PADDY水田/ALL',
    description     VARCHAR(300) NULL COMMENT '功能说明',
    sort_no         INT          NOT NULL DEFAULT 0,
    enabled         TINYINT      NOT NULL DEFAULT 1,
    INDEX idx_v3_machine_type_cat (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='农机小类字典';

-- -----------------------------------------------
-- 3. 作物品种字典（v3_crop）
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS v3_crop (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    code        VARCHAR(30)  NOT NULL UNIQUE,
    name        VARCHAR(50)  NOT NULL,
    alias       VARCHAR(100) NULL COMMENT '别名/俗称，如苞谷=玉米',
    crop_group  VARCHAR(30)  NOT NULL COMMENT '作物组：GRAIN粮食/OIL油料/FIBER纤维/VEG蔬菜/FRUIT水果/OTHER',
    -- 主产区省份代码，逗号分隔
    main_regions VARCHAR(200) NULL COMMENT '主产省份',
    -- 收获季节
    harvest_seasons VARCHAR(50) NULL COMMENT '收获季节月份范围，如5-6,9-10',
    sort_no     INT          NOT NULL DEFAULT 0,
    enabled     TINYINT      NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作物品种字典';

-- -----------------------------------------------
-- 4. 作业类型字典（v3_work_type）
--    农户选择"我需要什么服务"用此表
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS v3_work_type (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    code            VARCHAR(30)  NOT NULL UNIQUE,
    name            VARCHAR(50)  NOT NULL COMMENT '作业类型名称',
    category_id     INT          NULL COMMENT '对应农机大类（用于前端筛选）',
    suitable_crops  VARCHAR(200) NULL COMMENT '适用作物编码，*=通用，NULL=不限',
    description     VARCHAR(200) NULL,
    sort_no         INT          NOT NULL DEFAULT 0,
    enabled         TINYINT      NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作业类型字典';

-- -----------------------------------------------
-- 5. 行政区划表（v3_region）
--    四级：省(1)/市(2)/县(3)/乡镇(4)
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS v3_region (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    code        VARCHAR(12)  NOT NULL UNIQUE COMMENT '国标12位行政区划代码',
    name        VARCHAR(60)  NOT NULL COMMENT '区划名称',
    short_name  VARCHAR(30)  NULL COMMENT '简称，如"驻马店"',
    parent_code VARCHAR(12)  NULL COMMENT '上级区划代码',
    level       TINYINT      NOT NULL COMMENT '层级：1省 2市 3县 4乡镇',
    sort_no     INT          NOT NULL DEFAULT 0,
    INDEX idx_v3_region_parent (parent_code),
    INDEX idx_v3_region_level (level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行政区划表（四级）';

-- -----------------------------------------------
-- 6. 作业片区表（v3_zone）
--    最小业务单元，兼容行政村/社区/种植片区
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS v3_zone (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(80)  NOT NULL COMMENT '片区名称',
    zone_type       VARCHAR(20)  NOT NULL DEFAULT 'VILLAGE'
                    COMMENT '片区类型：VILLAGE行政村/COMMUNITY社区/FARM种植片区',
    township_code   VARCHAR(12)  NOT NULL COMMENT '所属乡镇区划代码',
    county_code     VARCHAR(12)  NOT NULL COMMENT '所属县区区划代码（冗余，便于查询）',
    city_code       VARCHAR(12)  NOT NULL COMMENT '所属市区划代码（冗余）',
    province_code   VARCHAR(12)  NOT NULL COMMENT '所属省区划代码（冗余）',
    description     VARCHAR(300) NULL COMMENT '片区简介',
    -- 农机数量统计（定期更新，缓存用）
    operator_count  INT          NOT NULL DEFAULT 0 COMMENT '注册农机手数量',
    machinery_count INT          NOT NULL DEFAULT 0 COMMENT '登记农机台数',
    -- 管理
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/INACTIVE',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_v3_zone_township (township_code),
    INDEX idx_v3_zone_county (county_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作业片区表';

-- -----------------------------------------------
-- 7. V3 用户表（v3_user）
--    单角色，绑定片区，复用 user_account 认证
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS v3_user (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    account_id      BIGINT       NOT NULL UNIQUE COMMENT '关联 user_account.id',
    role            VARCHAR(20)  NOT NULL COMMENT 'FARMER农户/OPERATOR农机手',
    real_name       VARCHAR(30)  NULL COMMENT '姓名（可选实名）',
    verified        TINYINT      NOT NULL DEFAULT 0 COMMENT '是否实名认证',
    zone_id         BIGINT       NULL COMMENT '所属片区ID',
    -- 农机手专属字段
    zone_joined_at  DATETIME     NULL COMMENT '加入片区时间',
    zone_changed_at DATETIME     NULL COMMENT '最近切换片区时间（冷却期判断）',
    -- 农户专属字段：家庭住址描述
    home_location   VARCHAR(200) NULL COMMENT '家庭位置描述（文字）',
    -- 联系虚拟号（由平台分配）
    virtual_phone   VARCHAR(20)  NULL COMMENT '虚拟联系号',
    -- 代操作支持：谁帮他注册/操作
    agent_account_id BIGINT      NULL COMMENT '代操作人 account_id（子女/亲属）',
    status          VARCHAR(20)  NOT NULL DEFAULT 'NORMAL',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_v3_user_zone (zone_id),
    INDEX idx_v3_user_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='V3用户表';

-- -----------------------------------------------
-- 8. 农机登记表（v3_machinery）
--    农机手登记的农业机械信息
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS v3_machinery (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    operator_id     BIGINT       NOT NULL COMMENT '农机手 v3_user.id',
    zone_id         BIGINT       NOT NULL COMMENT '所属片区 v3_zone.id',
    -- 机型信息
    machine_type_id INT          NOT NULL COMMENT 'v3_machine_type.id',
    brand           VARCHAR(60)  NULL COMMENT '品牌，如"雷沃谷神"/"沃得锐龙"/"久保田"/"中联重科"',
    model_no        VARCHAR(80)  NULL COMMENT '型号，如"GE80"/"4LZ-6.0ME"',
    -- 作业能力描述
    suitable_crops  VARCHAR(200) NULL COMMENT '实际能收的作物编码，逗号分隔',
    work_types      VARCHAR(200) NULL COMMENT '能提供的作业类型编码，逗号分隔',
    -- 跨区能力
    is_cross_region TINYINT      NOT NULL DEFAULT 0 COMMENT '是否提供跨区作业服务',
    cross_range_desc VARCHAR(200) NULL COMMENT '跨区描述，如"可跨省，5月河南6月山东"',
    -- 可作业时间
    avail_desc      VARCHAR(300) NULL COMMENT '可作业时间描述（自由文本）',
    -- 照片，JSON数组存URL
    photos          JSON         NULL COMMENT '机械照片URL列表',
    -- 附加说明
    description     VARCHAR(500) NULL COMMENT '其他说明，如"带秸秆打捆"',
    -- 状态
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/INACTIVE',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_v3_machinery_operator (operator_id),
    INDEX idx_v3_machinery_zone (zone_id),
    INDEX idx_v3_machinery_type (machine_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='农机登记表';

-- -----------------------------------------------
-- 9. 个人需求表（v3_demand）
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS v3_demand (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    farmer_id       BIGINT       NOT NULL COMMENT '发布者 v3_user.id',
    zone_id         BIGINT       NOT NULL COMMENT '所属片区',
    -- 作业信息
    work_type_id    INT          NOT NULL COMMENT 'v3_work_type.id',
    crop_id         INT          NULL COMMENT 'v3_crop.id，可为空',
    area_desc       VARCHAR(50)  NULL COMMENT '面积描述，如"约8亩"/"8-10亩"',
    expect_date_start DATE        NULL COMMENT '期望作业开始日期',
    expect_date_end   DATE        NULL COMMENT '期望作业结束日期',
    -- 地块信息
    location_desc   VARCHAR(300) NULL COMMENT '地块位置文字描述',
    plot_notes      VARCHAR(300) NULL COMMENT '地块注意事项，如"有水沟"',
    -- 照片
    photos          JSON         NULL COMMENT '地块照片URL列表',
    -- 合集关联
    group_id        BIGINT       NULL COMMENT '所属合集需求ID，NULL=独立需求',
    -- 状态：PUBLISHED/CONTACTED/COMPLETED/CANCELLED
    status          VARCHAR(20)  NOT NULL DEFAULT 'PUBLISHED',
    published_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_v3_demand_farmer (farmer_id),
    INDEX idx_v3_demand_zone (zone_id),
    INDEX idx_v3_demand_status (status),
    INDEX idx_v3_demand_group (group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='个人需求表';

-- -----------------------------------------------
-- 10. 合集需求表（v3_demand_group）
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS v3_demand_group (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    zone_id         BIGINT       NOT NULL COMMENT '所属片区',
    creator_id      BIGINT       NOT NULL COMMENT '发起人 v3_user.id',
    work_type_id    INT          NOT NULL COMMENT '作业类型',
    crop_id         INT          NULL COMMENT '作物品种',
    title           VARCHAR(100) NULL COMMENT '合集标题，如"南头地块统一旋耕"',
    total_area_desc VARCHAR(100) NULL COMMENT '合计面积估算描述',
    expect_date_start DATE        NULL,
    expect_date_end   DATE        NULL,
    location_desc   VARCHAR(300) NULL COMMENT '区域位置描述',
    member_count    INT          NOT NULL DEFAULT 1 COMMENT '已加入农户数',
    -- 状态：OPEN招募中/CLOSED关闭/COMPLETED已完成
    status          VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_v3_group_zone (zone_id),
    INDEX idx_v3_group_creator (creator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合集需求表';

-- -----------------------------------------------
-- 11. 合集需求成员表（v3_group_member）
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS v3_group_member (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id        BIGINT       NOT NULL COMMENT '合集需求ID',
    farmer_id       BIGINT       NOT NULL COMMENT '申请农户 v3_user.id',
    area_desc       VARCHAR(50)  NULL COMMENT '本人地块面积描述',
    location_desc   VARCHAR(200) NULL COMMENT '本人地块位置描述',
    -- 审批状态：PENDING/APPROVED/REJECTED
    join_status     VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    approved_by     BIGINT       NULL COMMENT '审批人 v3_user.id',
    approved_at     DATETIME     NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_group_farmer (group_id, farmer_id),
    INDEX idx_v3_member_group (group_id),
    INDEX idx_v3_member_farmer (farmer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合集需求成员表';


-- =============================================
-- 初始化种子数据
-- =============================================

-- -----------------------------------------------
-- 农机大类（参考国标NY/T 1640 + 补贴目录）
-- -----------------------------------------------
INSERT INTO v3_machine_category (code, name, icon, range_type, sort_no) VALUES
('HARVEST',      '收获机械',     '🌾', 'CROSS_PROVINCE', 10),
('TILLAGE',      '耕整地机械',   '🚜', 'LOCAL',          20),
('PLANTING',     '种植施肥机械', '🌱', 'LOCAL',          30),
('PLANT_PROT',   '植保机械',     '💊', 'LOCAL',          40),
('IRRIGATION',   '排灌机械',     '💧', 'LOCAL',          50),
('TRANSPORT',    '农用运输机械', '🚛', 'CROSS_COUNTY',   60),
('PROCESSING',   '农产品初加工', '⚙️', 'LOCAL',          70),
('FACILITY',     '设施农业装备', '🏡', 'LOCAL',          80),
('OTHER',        '其他农机',     '🔧', 'LOCAL',          90);


-- -----------------------------------------------
-- 农机小类（细化机型，注明跨区、季节、适用作物）
-- -----------------------------------------------
INSERT INTO v3_machine_type (category_id, code, name, alias, is_cross_region, is_seasonal, work_seasons, suitable_crops, terrain_type, description, sort_no) VALUES

-- === 收获机械 ===
-- 小麦联合收割机：经典跨省机器，每年5月从河南北上
(1, 'WHEAT_HARVESTER',  '小麦联合收割机', '麦收机,割麦机', 1, 1, 'SUMMER',
 'WHEAT', 'PLAIN',
 '收割小麦为主，部分机型换割台可收大豆、高粱。每年"三夏"（5-6月）从南向北跨省作业，享高速免费政策。', 10),

-- 谷物联合收割机（多作物）：主流轮式，雷沃谷神/中联主力
(1, 'GRAIN_HARVESTER',  '谷物联合收割机（多作物）', '联合收割机,收割机', 1, 1, 'SUMMER,AUTUMN',
 'WHEAT,RICE,SOYBEAN,SORGHUM,RAPESEED', 'PLAIN',
 '轮式为主，换割台可收小麦、大豆、高粱、油菜等多种旱地作物。雷沃谷神GE/GM系列、中联TG系列为代表，是跨省作业主力。', 11),

-- 水稻联合收割机（履带式）：南方水田主角
(1, 'RICE_HARVESTER',   '水稻联合收割机', '稻收机,打稻机', 1, 1, 'SUMMER,AUTUMN',
 'RICE', 'PADDY',
 '履带式为主，适应南方水田湿软地况。沃得锐龙、久保田PRO系列为代表。早稻7月、晚稻10-11月。部分机型可适配小麦割台。', 12),

-- 玉米收获机
(1, 'CORN_HARVESTER',   '玉米收获机', '掰棒子机,玉米机', 0, 1, 'AUTUMN',
 'CORN', 'PLAIN',
 '自走式玉米联合收获，集摘穗、剥皮、脱粒于一体。9-10月为主要作业季。多为本县或周边作业。', 13),

-- 花生收获机
(1, 'PEANUT_HARVESTER', '花生收获机', '花生机', 0, 1, 'AUTUMN',
 'PEANUT', 'PLAIN',
 '用于花生摘果、清选。9月为作业高峰，多为本地或县内作业。', 14),

-- 玉米大豆带状复合种植收获机（新型）
(1, 'COMPOUND_HARVESTER','玉米大豆复合收获机', '复合收获机', 0, 1, 'AUTUMN',
 'CORN,SOYBEAN', 'PLAIN',
 '适应玉米大豆带状复合种植模式（国家推广），一次作业同时收获两种作物。2022年后推广，西南/黄淮地区有应用。', 15),

-- 油菜收获机
(1, 'RAPESEED_HARVESTER','油菜收获机', '油菜机', 1, 1, 'SUMMER',
 'RAPESEED', 'PLAIN',
 '4-5月作业，长江流域为主产区，安徽、湖北、四川等省。部分联合收割机换油菜割台即可作业。', 16),

-- 甘蔗收获机
(1, 'SUGARCANE_HARVESTER','甘蔗收获机', '甘蔗机', 0, 1, 'AUTUMN,WINTER',
 'SUGARCANE', 'PLAIN',
 '11月-次年2月作业，集中在广西、云南、广东等南方蔗区，为本地或跨县作业。', 17),

-- 棉花收获机
(1, 'COTTON_PICKER',    '采棉机', '摘棉机,棉花机', 0, 1, 'AUTUMN',
 'COTTON', 'PLAIN',
 '9-11月作业，主要在新疆棉区大规模应用（机械化率极高），少量在黄淮棉区。', 18),

-- 茶叶采摘机
(1, 'TEA_HARVESTER',    '采茶机', '茶机', 0, 1, 'SPRING,AUTUMN',
 'TEA', 'HILL',
 '春茶（4月）、秋茶（9月）为作业旺季。浙江、安徽、福建、云南等茶区。丘陵为主。', 19),

-- 果蔬收获机械
(1, 'VEG_HARVESTER',    '蔬菜果品收获机', '收菜机', 0, 1, 'ALL',
 'VEG,TOMATO,POTATO', 'PLAIN',
 '包括番茄收获机、马铃薯收获机、辣椒收获机等，多为产地集中作业，本地为主。', 20),

-- 打捆机（秸秆处理）
(1, 'BALER',            '秸秆打捆机', '打捆机,捆草机', 0, 1, 'SUMMER,AUTUMN',
 '*', 'PLAIN',
 '粮食收获后秸秆打捆，供饲料/生物质利用。与收割机配套使用，本地或县内作业。', 21),

-- === 耕整地机械 ===
-- 旋耕机（最常见，几乎本地作业）
(2, 'ROTARY_TILLER',    '旋耕机', '旋地机,犁地机', 0, 1, 'SPRING,AUTUMN',
 '*', 'ALL',
 '最普及的土地整地机械，一年两次（春播前、秋收后）。豪丰、农哈哈、东方红配套拖拉机作业，本地为主。', 30),

-- 深松机
(2, 'SUBSOILER',        '深松机', '地松机', 0, 1, 'AUTUMN',
 '*', 'PLAIN',
 '打破犁底层，改善土壤通透性，每3年左右深松一次，秋收后作业，国家推广补贴。', 31),

-- 犁（铧式犁/圆盘犁）
(2, 'PLOW',             '铧式犁/圆盘犁', '犁', 0, 1, 'SPRING,AUTUMN',
 '*', 'ALL', '翻耕土地，与大功率拖拉机配套。', 32),

-- 微耕机（丘陵山区小地块专用）
(2, 'MICRO_TILLER',     '微耕机', '微型耕机,田园管理机', 0, 1, 'SPRING,AUTUMN',
 '*', 'HILL',
 '适合丘陵山区小地块、梯田，重量轻、灵活，西南地区广泛使用。', 33),

-- 水田整地机（机耕船）
(2, 'PADDY_TILLER',     '水田耕整机/机耕船', '机耕船', 0, 1, 'SPRING,SUMMER',
 'RICE', 'PADDY', '专用于水田泡田、耙地、起浆，南方双季稻区必备。', 34),

-- 起垄机
(2, 'RIDGER',           '起垄机/作畦机', '起垄机', 0, 1, 'SPRING',
 'PEANUT,CORN,VEG,SWEET_POTATO', 'PLAIN', '为花生、甘薯、蔬菜等作物起垄，春播前作业。', 35),

-- 铺膜机
(2, 'MULCH_LAYER',      '铺膜机', '覆膜机', 0, 1, 'SPRING',
 'CORN,PEANUT,VEG,COTTON', 'PLAIN', '春播前铺设地膜，西北、华北旱区常用。', 36),

-- === 种植施肥机械 ===
-- 小麦免耕播种机
(3, 'WHEAT_SEEDER',     '小麦播种机', '小麦种机,条播机', 0, 1, 'AUTUMN',
 'WHEAT', 'PLAIN', '9-10月小麦播种，免耕直播，一次性完成施肥+播种。', 40),

-- 玉米精量播种机
(3, 'CORN_SEEDER',      '玉米精量播种机', '玉米种机', 0, 1, 'SPRING,SUMMER',
 'CORN', 'PLAIN', '4-6月玉米播种，单粒精播，省种省工。', 41),

-- 水稻插秧机
(3, 'RICE_TRANSPLANTER','水稻插秧机', '插秧机', 0, 1, 'SPRING,SUMMER',
 'RICE', 'PADDY',
 '南方双季稻区早稻4-5月、晚稻7月，单季稻5-6月。久保田、洋马、井关为主流品牌。', 42),

-- 水稻直播机
(3, 'RICE_SEEDER',      '水稻直播机', '水稻直播机', 0, 1, 'SPRING,SUMMER',
 'RICE', 'PADDY', '代替插秧机，直接将稻种播入水田，省去育秧环节，推广中。', 43),

-- 施肥机（撒肥机）
(3, 'SPREADER',         '撒肥机/施肥机', '撒肥机', 0, 1, 'SPRING,AUTUMN',
 '*', 'ALL', '基肥、追肥施用，全年均可，播种前后为高峰期。', 44),

-- 大蒜播种机
(3, 'GARLIC_SEEDER',    '大蒜播种机', '种蒜机', 0, 1, 'AUTUMN',
 'GARLIC', 'PLAIN', '9-10月大蒜播种，山东、河南、江苏主产区需求旺盛。', 45),

-- === 植保机械 ===
-- 植保无人机（近年发展最快）
(4, 'DRONE_SPRAYER',    '植保无人机', '打药无人机,飞防无人机', 0, 1, 'SPRING,SUMMER,AUTUMN',
 '*', 'ALL',
 '极飞、大疆T系列为主流。全年可用，集中在苗期和抽穗期。丘陵、大地块均适用，跨区需求逐渐增加。', 50),

-- 喷杆喷雾机（大型地面植保）
(4, 'BOOM_SPRAYER',     '喷杆喷雾机', '打药机,大型植保机', 0, 1, 'SPRING,SUMMER',
 '*', 'PLAIN', '大幅宽地面植保，平原大地块高效，华北、东北较多。', 51),

-- 背负式动力喷雾机（小型，丘陵常用）
(4, 'KNAPSACK_SPRAYER', '背负式动力喷雾机', '背包打药机', 0, 1, 'SPRING,SUMMER',
 '*', 'HILL', '小地块、丘陵山区植保，操作灵活，但效率低，逐渐被无人机替代。', 52),

-- === 排灌机械 ===
(5, 'WATER_PUMP',       '农用水泵/抽水机', '抽水泵,灌溉泵', 0, 0, 'ALL',
 '*', 'ALL', '旱涝保收基础设施，季节性需求（旱季）或常年可用。', 60),

-- === 农用运输 ===
(6, 'AGRI_TRUCK',       '农用运输车/拖拉机运输', '三轮车,四轮车,拖拉机', 0, 0, 'ALL',
 '*', 'ALL', '农资、粮食运输，田间转运，本地为主。', 70),

-- === 农产品初加工 ===
(7, 'GRAIN_DRYER',      '粮食烘干机', '烘干机', 0, 1, 'AUTUMN',
 'WHEAT,RICE,CORN', 'ALL',
 '收获后粮食烘干，阴雨天气防霉变，9-11月需求旺盛，多为固定站点服务周边农户。', 80),

(7, 'THRESHER',         '脱粒机', '打场机', 0, 1, 'AUTUMN',
 'WHEAT,RICE,CORN,SOYBEAN', 'ALL', '小规模脱粒，适合山区小地块，平原已基本被联合收割机取代。', 81),

-- === 其他 ===
(9, 'TRACTOR',          '拖拉机（配套动力）', '大拖,小四轮', 0, 0, 'ALL',
 '*', 'ALL', '配套各种农机具的动力来源，东方红、雷沃、山拖为主流品牌。', 90);


-- -----------------------------------------------
-- 作物品种字典（覆盖中国主要作物）
-- -----------------------------------------------
INSERT INTO v3_crop (code, name, alias, crop_group, main_regions, harvest_seasons, sort_no) VALUES
-- 粮食作物
('WHEAT',       '小麦',   '冬小麦,春小麦',    'GRAIN', '河南,山东,河北,安徽,江苏,陕西,山西',      '5-6',       10),
('RICE',        '水稻',   '早稻,晚稻,中稻,粳稻,籼稻', 'GRAIN', '湖南,湖北,安徽,江苏,浙江,黑龙江,吉林,四川', '7,9-11',    11),
('CORN',        '玉米',   '苞谷,玉蜀黍,棒子', 'GRAIN', '东北,华北,西南,黄淮', '9-10',     12),
('SOYBEAN',     '大豆',   '黄豆',             'GRAIN', '黑龙江,内蒙古,吉林,安徽,河南', '9-10',     13),
('SORGHUM',     '高粱',   '红粮,蜀黍',        'GRAIN', '黑龙江,吉林,内蒙古,山西,河北', '9-10',     14),
('MILLET',      '谷子',   '小米,粟',          'GRAIN', '内蒙古,山西,河北,陕西', '9',         15),
('POTATO',      '马铃薯', '洋芋,土豆',        'GRAIN', '内蒙古,甘肃,四川,云南,贵州', '6-7,10',   16),
('SWEET_POTATO','甘薯',   '红薯,地瓜,番薯',   'GRAIN', '河南,安徽,山东,四川,广东', '9-11',     17),
-- 油料作物
('RAPESEED',    '油菜',   '油菜籽',           'OIL',   '湖北,安徽,四川,湖南,江苏', '4-5',       20),
('PEANUT',      '花生',   '落花生,结生',      'OIL',   '河南,山东,河北,广东,辽宁', '9',         21),
('SUNFLOWER',   '葵花',   '向日葵,葵瓜子',    'OIL',   '内蒙古,新疆,吉林,辽宁', '9-10',     22),
('SESAME',      '芝麻',   '脂麻',             'OIL',   '河南,湖北,安徽,江西', '8-9',       23),
-- 经济/纤维作物
('COTTON',      '棉花',   '皮棉',             'FIBER', '新疆,湖北,河南,山东,安徽', '9-11',     30),
('SUGARCANE',   '甘蔗',   '糖蔗',             'FIBER', '广西,云南,广东,海南', '11-3',      31),
('GARLIC',      '大蒜',   '蒜',               'VEG',   '山东,河南,江苏,云南,四川', '5,9-10',   40),
('GINGER',      '生姜',   '姜',               'VEG',   '山东,安徽,四川,贵州', '10-11',     41),
('TEA',         '茶叶',   '茶',               'OTHER', '浙江,福建,云南,安徽,四川', '3-5,9',     50),
('TOBACCO',     '烟草',   '烟叶',             'OTHER', '贵州,云南,河南,山东', '7-9',       51);


-- -----------------------------------------------
-- 作业类型字典（农户选择服务时用）
-- -----------------------------------------------
INSERT INTO v3_work_type (code, name, category_id, suitable_crops, description, sort_no) VALUES
-- 收获类
('HARVEST_WHEAT',   '收小麦',         1, 'WHEAT',                '联合收割机收获小麦',          10),
('HARVEST_RICE',    '收水稻',         1, 'RICE',                 '联合收割机收获水稻',          11),
('HARVEST_CORN',    '收玉米',         1, 'CORN',                 '玉米收获机摘穗或籽粒收获',    12),
('HARVEST_RAPESEED','收油菜',         1, 'RAPESEED',             '油菜联合收获',                13),
('HARVEST_SOYBEAN', '收大豆',         1, 'SOYBEAN',              '大豆联合收获',                14),
('HARVEST_PEANUT',  '收花生',         1, 'PEANUT',               '花生收获机摘果',              15),
('HARVEST_OTHER',   '其他作物收获',   1, '*',                    '甘蔗、棉花、蔬菜等专用收获',  16),
('BALING',          '秸秆打捆',       1, '*',                    '粮食收获后秸秆打捆处理',      17),
-- 耕整地类
('ROTARY_TILL',     '旋耕',           2, '*',                    '旋耕机整地，播前准备',        20),
('DEEP_LOOSEN',     '深松',           2, '*',                    '深松机打破犁底层',            21),
('PLOW',            '犁地/翻地',      2, '*',                    '铧式犁或圆盘犁翻耕土地',      22),
('RIDGE',           '起垄',           2, 'PEANUT,CORN,VEG',      '起垄机做垄形',                23),
('MULCH',           '铺膜',           2, 'CORN,PEANUT,VEG',      '地膜覆盖保墒保温',            24),
('PADDY_PREP',      '水田整地/泡田',  2, 'RICE',                 '水田泡田、耙地、起浆',        25),
-- 种植类
('SEED_WHEAT',      '小麦播种',       3, 'WHEAT',                '条播机或免耕播种机播种',      30),
('SEED_CORN',       '玉米播种',       3, 'CORN',                 '精量播种机播种',              31),
('TRANSPLANT_RICE', '水稻插秧',       3, 'RICE',                 '插秧机插秧',                  32),
('DIRECT_RICE',     '水稻直播',       3, 'RICE',                 '水稻直播机直播',              33),
('SEED_OTHER',      '其他播种',       3, '*',                    '大蒜、大豆等其他作物播种',    34),
('FERTILIZE',       '机械施肥',       3, '*',                    '撒肥机撒施基肥或追肥',        35),
-- 植保类
('SPRAY_DRONE',     '无人机飞防',     4, '*',                    '植保无人机喷药防治病虫害',    40),
('SPRAY_GROUND',    '地面植保喷药',   4, '*',                    '喷杆喷雾机或背负式喷药',      41),
-- 其他
('IRRIGATION',      '灌溉抽水',       5, '*',                    '水泵抽水灌溉或排涝',          50),
('TRANSPORT',       '农产品运输',     6, '*',                    '农用车粮食或农资运输',        60),
('GRAIN_DRY',       '粮食烘干',       7, 'WHEAT,RICE,CORN',      '烘干机烘干潮粮',              70);


-- -----------------------------------------------
-- 初始化行政区划（河南省 → 驻马店市 → 西平县 → 盆尧镇）
-- 用于开发测试，正式上线前补充完整数据
-- -----------------------------------------------
INSERT INTO v3_region (code, name, short_name, parent_code, level, sort_no) VALUES
-- 省级
('410000000000', '河南省',     '河南', NULL,           1, 1),
-- 市级
('411700000000', '驻马店市',   '驻马店', '410000000000', 2, 1),
-- 县级
('411722000000', '西平县',     '西平', '411700000000', 3, 1),
-- 乡镇级
('411722100000', '盆尧镇',     '盆尧', '411722000000', 4, 1),
('411722101000', '西平县城关镇','城关', '411722000000', 4, 2),
('411722102000', '出山镇',     '出山', '411722000000', 4, 3);

-- 初始化测试片区
INSERT INTO v3_zone (name, zone_type, township_code, county_code, city_code, province_code, description) VALUES
('盆尧镇中心村',   'VILLAGE',   '411722100000', '411722000000', '411700000000', '410000000000', '盆尧镇主要粮食作物区，以小麦、玉米轮作为主'),
('盆尧镇北片区',   'VILLAGE',   '411722100000', '411722000000', '411700000000', '410000000000', '盆尧镇北部地块集中区'),
('西平县农业示范区','FARM',     '411722101000', '411722000000', '411700000000', '410000000000', '西平县现代农业示范种植基地');
