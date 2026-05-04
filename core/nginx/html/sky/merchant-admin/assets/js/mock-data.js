// Mock Data for Campus Takeout Admin System

// 商品分类
const categories = [
    { id: 1, name: '热菜', icon: '🔥' },
    { id: 2, name: '凉菜', icon: '🥗' },
    { id: 3, name: '主食', icon: '🍚' },
    { id: 4, name: '汤类', icon: '🍜' }
];

// 商品数据
const products = [
    {
        id: 1,
        name: '宫保鸡丁',
        category: '热菜',
        categoryId: 1,
        price: 38.00,
        image: './download/product_gongbao_chicken.jpg',
        description: '川菜经典，鸡肉鲜嫩，花生酥脆，酸甜微辣',
        status: 1,
        sales: 328,
        stock: 50
    },
    {
        id: 2,
        name: '鱼香肉丝',
        category: '热菜',
        categoryId: 1,
        price: 32.00,
        image: './download/product_yuxiang_pork.jpg',
        description: '经典川菜，肉丝嫩滑，鱼香浓郁',
        status: 1,
        sales: 256,
        stock: 45
    },
    {
        id: 3,
        name: '糖醋排骨',
        category: '热菜',
        categoryId: 1,
        price: 48.00,
        image: './download/product_sweet_sour_pork.jpg',
        description: '外酥里嫩，酸甜可口，色泽诱人',
        status: 1,
        sales: 412,
        stock: 30
    },
    {
        id: 4,
        name: '麻婆豆腐',
        category: '热菜',
        categoryId: 1,
        price: 26.00,
        image: './download/product_mapo_tofu.jpg',
        description: '麻辣鲜香，豆腐嫩滑，经典川味',
        status: 1,
        sales: 289,
        stock: 60
    },
    {
        id: 5,
        name: '红烧茄子',
        category: '热菜',
        categoryId: 1,
        price: 24.00,
        image: './download/product_braised_eggplant.jpg',
        description: '茄子软糯，酱香浓郁，下饭佳品',
        status: 1,
        sales: 178,
        stock: 40
    },
    {
        id: 6,
        name: '干煸豆角',
        category: '热菜',
        categoryId: 1,
        price: 28.00,
        image: './download/product_green_beans.jpg',
        description: '豆角脆嫩，干香四溢',
        status: 1,
        sales: 156,
        stock: 35
    },
    {
        id: 7,
        name: '拍黄瓜',
        category: '凉菜',
        categoryId: 2,
        price: 12.00,
        image: './download/product_cucumber_salad.jpg',
        description: '清爽开胃，蒜香浓郁',
        status: 1,
        sales: 423,
        stock: 80
    },
    {
        id: 8,
        name: '凉拌木耳',
        category: '凉菜',
        categoryId: 2,
        price: 16.00,
        image: './download/product_wood_ear_salad.jpg',
        description: '脆爽可口，营养丰富',
        status: 1,
        sales: 267,
        stock: 55
    },
    {
        id: 9,
        name: '春卷',
        category: '凉菜',
        categoryId: 2,
        price: 18.00,
        image: './download/product_spring_rolls.jpg',
        description: '外皮酥脆，内馅鲜美',
        status: 1,
        sales: 198,
        stock: 40
    },
    {
        id: 10,
        name: '扬州炒饭',
        category: '主食',
        categoryId: 3,
        price: 22.00,
        image: './download/product_fried_rice.jpg',
        description: '粒粒分明，料足味美',
        status: 1,
        sales: 512,
        stock: 70
    },
    {
        id: 11,
        name: '红烧牛肉面',
        category: '主食',
        categoryId: 3,
        price: 28.00,
        image: './download/product_noodles.jpg',
        description: '牛肉酥烂，汤浓味厚',
        status: 1,
        sales: 387,
        stock: 45
    },
    {
        id: 12,
        name: '猪肉水饺',
        category: '主食',
        categoryId: 3,
        price: 20.00,
        image: './download/product_dumplings.jpg',
        description: '皮薄馅大，鲜美多汁',
        status: 1,
        sales: 298,
        stock: 60
    },
    {
        id: 13,
        name: '白米饭',
        category: '主食',
        categoryId: 3,
        price: 3.00,
        image: './download/product_white_rice.jpg',
        description: '香软可口，粒粒饱满',
        status: 1,
        sales: 856,
        stock: 200
    },
    {
        id: 14,
        name: '番茄蛋花汤',
        category: '汤类',
        categoryId: 4,
        price: 12.00,
        image: './download/product_tomato_egg_soup.jpg',
        description: '酸甜开胃，营养丰富',
        status: 1,
        sales: 445,
        stock: 90
    },
    {
        id: 15,
        name: '紫菜蛋花汤',
        category: '汤类',
        categoryId: 4,
        price: 10.00,
        image: './download/product_seaweed_soup.jpg',
        description: '清淡鲜美，简单美味',
        status: 1,
        sales: 378,
        stock: 85
    }
];

// 订单数据
const orders = [
    {
        id: 'ORD20260210001',
        customer: '张先生',
        phone: '138****5678',
        address: '朝阳区望京SOHO T3 1208室',
        items: [
            { productId: 1, name: '宫保鸡丁', price: 38.00, quantity: 1, image: './download/product_gongbao_chicken.jpg' },
            { productId: 10, name: '扬州炒饭', price: 22.00, quantity: 1, image: './download/product_fried_rice.jpg' },
            { productId: 14, name: '番茄蛋花汤', price: 12.00, quantity: 1, image: './download/product_tomato_egg_soup.jpg' }
        ],
        totalAmount: 72.00,
        status: 'pending',
        orderTime: '2026-02-10 14:30:22',
        note: '少辣，不要葱'
    },
    {
        id: 'ORD20260210002',
        customer: '李女士',
        phone: '139****1234',
        address: '海淀区中关村创业大街12号',
        items: [
            { productId: 3, name: '糖醋排骨', price: 48.00, quantity: 1, image: './download/product_sweet_sour_pork.jpg' },
            { productId: 7, name: '拍黄瓜', price: 12.00, quantity: 2, image: './download/product_cucumber_salad.jpg' },
            { productId: 13, name: '白米饭', price: 3.00, quantity: 2, image: './download/product_white_rice.jpg' }
        ],
        totalAmount: 78.00,
        status: 'preparing',
        orderTime: '2026-02-10 14:15:08',
        note: ''
    },
    {
        id: 'ORD20260210003',
        customer: '王先生',
        phone: '136****8888',
        address: '东城区建国门外大街1号',
        items: [
            { productId: 11, name: '红烧牛肉面', price: 28.00, quantity: 2, image: './download/product_noodles.jpg' },
            { productId: 9, name: '春卷', price: 18.00, quantity: 1, image: './download/product_spring_rolls.jpg' }
        ],
        totalAmount: 74.00,
        status: 'delivering',
        orderTime: '2026-02-10 13:58:33',
        note: '送到前台即可'
    },
    {
        id: 'ORD20260210004',
        customer: '赵女士',
        phone: '137****6666',
        address: '西城区金融街35号',
        items: [
            { productId: 4, name: '麻婆豆腐', price: 26.00, quantity: 1, image: './download/product_mapo_tofu.jpg' },
            { productId: 10, name: '扬州炒饭', price: 22.00, quantity: 1, image: './download/product_fried_rice.jpg' },
            { productId: 15, name: '紫菜蛋花汤', price: 10.00, quantity: 1, image: './download/product_seaweed_soup.jpg' }
        ],
        totalAmount: 58.00,
        status: 'completed',
        orderTime: '2026-02-10 13:25:15',
        note: ''
    },
    {
        id: 'ORD20260210005',
        customer: '刘先生',
        phone: '135****9999',
        address: '朝阳区三里屯路19号',
        items: [
            { productId: 2, name: '鱼香肉丝', price: 32.00, quantity: 1, image: './download/product_yuxiang_pork.jpg' },
            { productId: 5, name: '红烧茄子', price: 24.00, quantity: 1, image: './download/product_braised_eggplant.jpg' },
            { productId: 13, name: '白米饭', price: 3.00, quantity: 2, image: './download/product_white_rice.jpg' }
        ],
        totalAmount: 62.00,
        status: 'completed',
        orderTime: '2026-02-10 12:45:50',
        note: '多给点米饭'
    },
    {
        id: 'ORD20260210006',
        customer: '陈女士',
        phone: '133****7777',
        address: '海淀区上地信息路2号',
        items: [
            { productId: 12, name: '猪肉水饺', price: 20.00, quantity: 2, image: './download/product_dumplings.jpg' },
            { productId: 8, name: '凉拌木耳', price: 16.00, quantity: 1, image: './download/product_wood_ear_salad.jpg' }
        ],
        totalAmount: 56.00,
        status: 'pending',
        orderTime: '2026-02-10 14:35:12',
        note: '水饺要醋和辣椒'
    }
];

// 套餐数据
const setMeals = [
    {
        id: 1,
        name: '经典双人套餐',
        price: 88.00,
        originalPrice: 108.00,
        image: './download/product_gongbao_chicken.jpg',
        description: '宫保鸡丁 + 拍黄瓜 + 扬州炒饭 + 番茄蛋花汤',
        products: [
            { id: 1, name: '宫保鸡丁', quantity: 1 },
            { id: 7, name: '拍黄瓜', quantity: 1 },
            { id: 10, name: '扬州炒饭', quantity: 1 },
            { id: 14, name: '番茄蛋花汤', quantity: 1 }
        ],
        status: 1,
        sales: 156
    },
    {
        id: 2,
        name: '川味特色套餐',
        price: 98.00,
        originalPrice: 118.00,
        image: './download/product_mapo_tofu.jpg',
        description: '麻婆豆腐 + 鱼香肉丝 + 白米饭 + 紫菜蛋花汤',
        products: [
            { id: 4, name: '麻婆豆腐', quantity: 1 },
            { id: 2, name: '鱼香肉丝', quantity: 1 },
            { id: 13, name: '白米饭', quantity: 2 },
            { id: 15, name: '紫菜蛋花汤', quantity: 1 }
        ],
        status: 1,
        sales: 123
    },
    {
        id: 3,
        name: '工作餐套餐',
        price: 38.00,
        originalPrice: 48.00,
        image: './download/product_fried_rice.jpg',
        description: '扬州炒饭 + 拍黄瓜 + 紫菜蛋花汤',
        products: [
            { id: 10, name: '扬州炒饭', quantity: 1 },
            { id: 7, name: '拍黄瓜', quantity: 1 },
            { id: 15, name: '紫菜蛋花汤', quantity: 1 }
        ],
        status: 1,
        sales: 287
    }
];

// 统计数据
const statistics = {
    today: {
        orders: 156,
        revenue: 8567.00,
        pendingOrders: 12,
        totalProducts: 15
    },
    trend: {
        ordersChange: '+12.5%',
        revenueChange: '+8.3%',
        pendingChange: '+3',
        productsChange: '+2'
    },
    // 7天销售数据
    weekData: {
        labels: ['2/4', '2/5', '2/6', '2/7', '2/8', '2/9', '2/10'],
        revenue: [6800, 7200, 6500, 7800, 7100, 8200, 8567],
        orders: [128, 145, 132, 156, 142, 168, 156]
    },
    // 分类销售占比
    categoryData: {
        labels: ['热菜', '凉菜', '主食', '汤类'],
        values: [45, 15, 30, 10],
        colors: ['#FF6B35', '#FFA500', '#52C41A', '#13C2C2']
    },
    // 热销商品 Top 10
    topProducts: [
        { name: '白米饭', sales: 856, percentage: 100 },
        { name: '扬州炒饭', sales: 512, percentage: 60 },
        { name: '番茄蛋花汤', sales: 445, percentage: 52 },
        { name: '拍黄瓜', sales: 423, percentage: 49 },
        { name: '糖醋排骨', sales: 412, percentage: 48 },
        { name: '红烧牛肉面', sales: 387, percentage: 45 },
        { name: '紫菜蛋花汤', sales: 378, percentage: 44 },
        { name: '宫保鸡丁', sales: 328, percentage: 38 },
        { name: '猪肉水饺', sales: 298, percentage: 35 },
        { name: '麻婆豆腐', sales: 289, percentage: 34 }
    ],
    // 时段分析（每小时订单量）
    hourlyOrders: {
        labels: ['0-1', '1-2', '2-3', '3-4', '4-5', '5-6', '6-7', '7-8', '8-9', '9-10', '10-11', '11-12',
                 '12-13', '13-14', '14-15', '15-16', '16-17', '17-18', '18-19', '19-20', '20-21', '21-22', '22-23', '23-24'],
        values: [2, 1, 0, 0, 1, 3, 8, 15, 12, 18, 25, 45, 68, 52, 38, 22, 28, 42, 58, 35, 18, 12, 6, 3]
    }
};

// 员工数据
const employees = [
    {
        id: 1,
        name: '张伟',
        phone: '138****1234',
        role: '店长',
        status: 1,
        createTime: '2024-01-15'
    },
    {
        id: 2,
        name: '李娜',
        phone: '139****5678',
        role: '厨师长',
        status: 1,
        createTime: '2024-02-20'
    },
    {
        id: 3,
        name: '王强',
        phone: '136****9999',
        role: '厨师',
        status: 1,
        createTime: '2024-03-10'
    },
    {
        id: 4,
        name: '刘芳',
        phone: '137****6666',
        role: '服务员',
        status: 1,
        createTime: '2024-04-05'
    },
    {
        id: 5,
        name: '陈明',
        phone: '135****8888',
        role: '配送员',
        status: 0,
        createTime: '2024-05-12'
    }
];

// 用户数据
const users = [
    {
        username: 'admin',
        password: '123456',
        name: '管理员',
        role: 'admin'
    },
    {
        username: 'test',
        password: '123456',
        name: '测试用户',
        role: 'user'
    }
];

// 导出数据
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        categories,
        products,
        orders,
        setMeals,
        statistics,
        employees,
        users
    };
}