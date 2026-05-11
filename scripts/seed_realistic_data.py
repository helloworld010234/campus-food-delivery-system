#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Seed realistic campus food delivery data for thesis validation.
Targets: Nantong University Xinglin College students.
"""

import random
import datetime
import pymysql
from decimal import Decimal, ROUND_HALF_UP

DB_CONFIG = {
    "host": "localhost",
    "port": 3306,
    "user": "root",
    "password": "root",
    "database": "sky_take_out",
    "charset": "utf8mb4"
}

# ========== Student personas ==========
STUDENTS = [
    {"name": "张宇航", "sex": "1", "major": "物联网工程", "phone": "18751321001"},
    {"name": "李思涵", "sex": "2", "major": "护理学", "phone": "18751331002"},
    {"name": "王浩然", "sex": "1", "major": "临床医学", "phone": "18751341003"},
    {"name": "陈雨萱", "sex": "2", "major": "药学", "phone": "18751351004"},
    {"name": "刘子轩", "sex": "1", "major": "电子信息工程", "phone": "18751361005"},
    {"name": "赵欣怡", "sex": "2", "major": "会计学", "phone": "18751371006"},
    {"name": "孙博文", "sex": "1", "major": "土木工程", "phone": "18751381007"},
    {"name": "周梦琪", "sex": "2", "major": "汉语言文学", "phone": "18751391008"},
]

# ========== Address templates ==========
ADDRESS_TEMPLATES = [
    {"detail": "1号宿舍楼 301 室", "label": "宿舍", "consignee": None},
    {"detail": "1号宿舍楼 402 室", "label": "宿舍", "consignee": None},
    {"detail": "2号宿舍楼 205 室", "label": "宿舍", "consignee": None},
    {"detail": "2号宿舍楼 608 室", "label": "宿舍", "consignee": None},
    {"detail": "3号宿舍楼 101 室", "label": "宿舍", "consignee": None},
    {"detail": "3号宿舍楼 510 室", "label": "宿舍", "consignee": None},
    {"detail": "4号宿舍楼 308 室", "label": "宿舍", "consignee": None},
    {"detail": "4号宿舍楼 702 室", "label": "宿舍", "consignee": None},
    {"detail": "教学楼 A 座 302 教室", "label": "教室", "consignee": None},
    {"detail": "教学楼 B 座 105 教室", "label": "教室", "consignee": None},
    {"detail": "图书馆 3 楼自习区", "label": "图书馆", "consignee": None},
    {"detail": "图书馆 5 楼阅览室", "label": "图书馆", "consignee": None},
]

# Shared address fields
PROVINCE_CODE = "320000"
PROVINCE_NAME = "江苏省"
CITY_CODE = "320600"
CITY_NAME = "南通市"
DISTRICT_CODE = "320602"
DISTRICT_NAME = "崇川区"

# ========== Order config ==========
ORDER_COUNT = 60
DATE_RANGE_DAYS = 30
MERCHANT_IDS = [1, 2]
MERCHANT_NAMES = {1: "杏林食速", 2: "杏林食堂"}

# Status distribution (matches design)
STATUS_POOL = (
    [5] * 42      # 已完成
    + [6] * 8     # 已取消
    + [2] * 4     # 待接单
    + [3] * 3     # 已接单
    + [4] * 3     # 派送中
)

# Meal time distribution weights
MEAL_WEIGHTS = [
    ("breakfast", 0.10, 7, 0, 8, 30),
    ("lunch", 0.40, 11, 20, 12, 50),
    ("dinner", 0.45, 17, 10, 18, 40),
    ("night", 0.05, 21, 0, 22, 30),
]

CANCEL_REASONS = [
    "下单后改变主意，不想要了",
    "等太久，已经去食堂吃了",
    "发现地址填错了，重新下单",
    "临时有事，不需要送餐了",
    "菜品售罄，系统自动取消",
]

REMARKS = [
    "",
    "少放辣，谢谢",
    "送到宿舍楼下打电话",
    "加急，马上上课了",
    "米饭多一点",
    "不要香菜",
    "送到图书馆南门",
    "电话联系 1875132xxxx",
    "谢谢配送小哥",
    "尽快送达",
]


def connect_db():
    return pymysql.connect(**DB_CONFIG)


def clear_existing_data(cursor):
    """Remove existing test orders/users/addresses while keeping dishes/categories/employees."""
    print("Clearing existing test data...")
    cursor.execute("SET FOREIGN_KEY_CHECKS = 0;")
    cursor.execute("DELETE FROM order_detail;")
    cursor.execute("DELETE FROM orders;")
    cursor.execute("DELETE FROM address_book;")
    cursor.execute("DELETE FROM user;")
    cursor.execute("ALTER TABLE order_detail AUTO_INCREMENT = 1;")
    cursor.execute("ALTER TABLE orders AUTO_INCREMENT = 1;")
    cursor.execute("ALTER TABLE address_book AUTO_INCREMENT = 1;")
    cursor.execute("ALTER TABLE user AUTO_INCREMENT = 1;")
    cursor.execute("SET FOREIGN_KEY_CHECKS = 1;")
    print("Cleared.")


def fetch_dishes(cursor):
    cursor.execute("SELECT id, name, price FROM dish WHERE status = 1;")
    return cursor.fetchall()


def generate_users(cursor):
    print("Generating users...")
    user_ids = []
    for idx, s in enumerate(STUDENTS, start=1):
        openid = f"student_2026_{s['phone'][-4:]}"
        sql = """
            INSERT INTO user (openid, name, phone, sex, create_time)
            VALUES (%s, %s, %s, %s, NOW());
        """
        cursor.execute(sql, (openid, s["name"], s["phone"], s["sex"]))
        user_ids.append(cursor.lastrowid)
    print(f"Generated {len(user_ids)} users.")
    return user_ids


def generate_addresses(cursor, user_ids):
    print("Generating addresses...")
    addr_ids = []
    # Assign 1-2 addresses per user
    templates = [dict(t) for t in ADDRESS_TEMPLATES]
    random.shuffle(templates)
    used = 0
    for uid in user_ids:
        count = random.choice([1, 2])
        for _ in range(count):
            if used >= len(templates):
                break
            tpl = templates[used]
            used += 1
            # Find student name for consignee
            student_name = None
            cursor.execute("SELECT name FROM user WHERE id = %s", (uid,))
            row = cursor.fetchone()
            if row:
                student_name = row[0]
            consignee = tpl["consignee"] or student_name or "同学"
            is_default = 1 if _ == 0 else 0
            sql = """
                INSERT INTO address_book
                (user_id, consignee, sex, phone, province_code, province_name,
                 city_code, city_name, district_code, district_name, detail, label, is_default)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s);
            """
            cursor.execute(sql, (
                uid, consignee, random.choice(["1", "2"]),
                random.choice(["18751321001", "18751331002", "18751341003", "18751351004",
                               "18751361005", "18751371006", "18751381007", "18751391008"]),
                PROVINCE_CODE, PROVINCE_NAME, CITY_CODE, CITY_NAME,
                DISTRICT_CODE, DISTRICT_NAME, tpl["detail"], tpl["label"], is_default
            ))
            addr_ids.append(cursor.lastrowid)
    print(f"Generated {len(addr_ids)} addresses.")
    return addr_ids


def pick_order_time(base_date):
    """Pick a realistic order time based on meal distribution."""
    r = random.random()
    cumulative = 0.0
    for name, weight, sh, sm, eh, em in MEAL_WEIGHTS:
        cumulative += weight
        if r <= cumulative:
            start = datetime.time(sh, sm)
            end = datetime.time(eh, em)
            break
    else:
        start = datetime.time(17, 10)
        end = datetime.time(18, 40)

    start_min = start.hour * 60 + start.minute
    end_min = end.hour * 60 + end.minute
    picked_min = random.randint(start_min, end_min)
    picked_time = datetime.time(picked_min // 60, picked_min % 60)
    return datetime.datetime.combine(base_date, picked_time)


def generate_order_number(order_time, seq):
    ts = order_time.strftime("%Y%m%d%H%M%S")
    return f"{ts}{seq:04d}"


def generate_orders(cursor, user_ids, addr_ids, dishes):
    print("Generating orders...")
    # Pre-assign addresses to users for consistency
    user_addrs = {}
    for uid in user_ids:
        cursor.execute("SELECT id FROM address_book WHERE user_id = %s", (uid,))
        rows = cursor.fetchall()
        user_addrs[uid] = [r[0] for r in rows]

    today = datetime.date.today()
    order_ids = []
    statuses = STATUS_POOL.copy()
    random.shuffle(statuses)

    for i in range(ORDER_COUNT):
        user_id = random.choice(user_ids)
        addr_id = random.choice(user_addrs[user_id])
        merchant_id = random.choices(MERCHANT_IDS, weights=[67, 33])[0]
        status = statuses[i]

        # Date: weighted toward recent days, weekdays preferred
        day_offset = random.choices(range(DATE_RANGE_DAYS), weights=[max(1, d + 1) for d in range(DATE_RANGE_DAYS)])[0]
        base_date = today - datetime.timedelta(days=day_offset)
        # Bias toward Mon-Fri
        if base_date.weekday() >= 5 and random.random() < 0.5:
            base_date = base_date - datetime.timedelta(days=random.choice([1, 2]))

        order_time = pick_order_time(base_date)
        order_number = generate_order_number(order_time, i + 1)

        # Checkout time: 1-5 min after order
        checkout_time = order_time + datetime.timedelta(seconds=random.randint(30, 300)) if status >= 2 else None

        # Pay status
        pay_status = 1 if status >= 2 else 0

        # Cancel logic
        cancel_time = None
        cancel_reason = None
        rejection_reason = None
        if status == 6:
            cancel_time = order_time + datetime.timedelta(minutes=random.randint(3, 15))
            cancel_reason = random.choice(CANCEL_REASONS)

        # Delivery time for completed orders
        delivery_time = None
        estimated_delivery_time = None
        if status in (4, 5):
            estimated_delivery_time = order_time + datetime.timedelta(minutes=random.randint(25, 45))
        if status == 5:
            delivery_time = order_time + datetime.timedelta(minutes=random.randint(20, 50))

        # Build items
        item_count = random.choices([1, 2, 3, 4], weights=[25, 40, 25, 10])[0]
        selected_dishes = random.sample(dishes, min(item_count, len(dishes)))
        goods_amount = Decimal("0")
        order_details = []
        for d in selected_dishes:
            qty = random.randint(1, 2)
            unit = Decimal(str(d[2]))
            subtotal = (unit * qty).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)
            goods_amount += subtotal
            order_details.append({
                "dish_id": d[0],
                "name": d[1],
                "image": "",  # optional
                "number": qty,
                "amount": subtotal,
            })

        delivery_fee = Decimal("3.00")
        amount = (goods_amount + delivery_fee).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)
        remark = random.choice(REMARKS)
        phone = random.choice(["18751321001", "18751331002", "18751341003", "18751351004",
                               "18751361005", "18751371006", "18751381007", "18751391008"])

        # Get user name for order
        cursor.execute("SELECT name FROM user WHERE id = %s", (user_id,))
        user_name = cursor.fetchone()[0]

        sql = """
            INSERT INTO orders
            (campus_id, merchant_id, merchant_name, number, status, user_id, address_book_id,
             order_time, checkout_time, pay_method, pay_status, amount, goods_amount, delivery_fee,
             remark, phone, address, user_name, consignee, cancel_reason, rejection_reason,
             cancel_time, estimated_delivery_time, delivery_status, delivery_time, item_count)
            VALUES
            (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s);
        """
        # Build full address text
        cursor.execute("SELECT detail FROM address_book WHERE id = %s", (addr_id,))
        detail = cursor.fetchone()[0]
        full_address = f"{PROVINCE_NAME}{CITY_NAME}{DISTRICT_NAME}啬园路9号南通大学杏林学院{detail}"

        cursor.execute(sql, (
            1, merchant_id, MERCHANT_NAMES[merchant_id], order_number, status, user_id, addr_id,
            order_time, checkout_time, 1, pay_status, amount, goods_amount, delivery_fee,
            remark, phone, full_address, user_name, user_name,
            cancel_reason, rejection_reason, cancel_time, estimated_delivery_time,
            1, delivery_time, len(order_details)
        ))
        order_id = cursor.lastrowid
        order_ids.append(order_id)

        # Insert order details
        for od in order_details:
            cursor.execute("""
                INSERT INTO order_detail (name, image, order_id, dish_id, number, amount)
                VALUES (%s, %s, %s, %s, %s, %s);
            """, (od["name"], od["image"], order_id, od["dish_id"], od["number"], od["amount"]))

    print(f"Generated {len(order_ids)} orders with details.")
    return order_ids


def print_summary(cursor):
    print("\n========== Data Summary ==========")
    for table in ["user", "address_book", "orders", "order_detail"]:
        cursor.execute(f"SELECT COUNT(*) FROM {table}")
        count = cursor.fetchone()[0]
        print(f"  {table}: {count} rows")

    cursor.execute("""
        SELECT status, COUNT(*) FROM orders GROUP BY status ORDER BY status;
    """)
    print("\n  Order status distribution:")
    status_names = {1: "待付款", 2: "待接单", 3: "已接单", 4: "派送中", 5: "已完成", 6: "已取消", 7: "退款"}
    for status, cnt in cursor.fetchall():
        print(f"    {status_names.get(status, status)}: {cnt}")

    cursor.execute("""
        SELECT merchant_name, COUNT(*), SUM(amount) FROM orders GROUP BY merchant_name;
    """)
    print("\n  Merchant distribution:")
    for name, cnt, total in cursor.fetchall():
        print(f"    {name}: {cnt} orders, CNY {total}")

    cursor.execute("""
        SELECT DATE(order_time) as d, COUNT(*), SUM(amount)
        FROM orders GROUP BY d ORDER BY d DESC LIMIT 7;
    """)
    print("\n  Last 7 days:")
    for d, cnt, total in cursor.fetchall():
        print(f"    {d}: {cnt} orders, CNY {total}")


def main():
    conn = connect_db()
    try:
        with conn.cursor() as cursor:
            clear_existing_data(cursor)
            dishes = fetch_dishes(cursor)
            print(f"Available dishes: {len(dishes)}")
            user_ids = generate_users(cursor)
            addr_ids = generate_addresses(cursor, user_ids)
            generate_orders(cursor, user_ids, addr_ids, dishes)
            print_summary(cursor)
            conn.commit()
            print("\nAll data committed successfully.")
    except Exception as e:
        conn.rollback()
        print(f"ERROR: {e}")
        raise
    finally:
        conn.close()


if __name__ == "__main__":
    main()
