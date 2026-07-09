import urllib.request, json, sys

BASE = "http://localhost:8080"

def api(method, path, data=None):
    url = f"{BASE}{path}"
    body = json.dumps(data).encode('utf-8') if data else None
    req = urllib.request.Request(url, data=body, headers={'Content-Type': 'application/json'} if body else {})
    req.get_method = lambda: method
    resp = urllib.request.urlopen(req)
    return json.loads(resp.read())

# Login & create order
login = api("POST", "/api/login", {"username": "admin", "password": "admin123"})

order = api("POST", "/api/production-orders", {"productName": "test box", "spec": "40x30", "qty": 100})
oid = order["data"]["id"]
print(f"1. 新建生产单 ID={oid} 状态={order['data']['status']}")

# Test skip (模切区 before 印刷区)
r = api("POST", "/api/public/report", {"productionOrderId": oid, "workshop": "模切区", "outputQty": 10, "wasteQty": 0, "operator": "张三"})
print(f"2. 跳过印刷区直接报模切区: CODE={r.get('code')}")

# 印刷区
api("POST", "/api/public/report", {"productionOrderId": oid, "workshop": "印刷区", "outputQty": 20, "wasteQty": 0, "operator": "张三"})
s = api("GET", f"/api/public/production-order/{oid}")["data"]["status"]
print(f"3. 印刷区报工 → {s} {'✅' if s == '模切' else '❌ 期望模切'}")

# 模切区
api("POST", "/api/public/report", {"productionOrderId": oid, "workshop": "模切区", "outputQty": 20, "wasteQty": 0, "operator": "李四"})
s = api("GET", f"/api/public/production-order/{oid}")["data"]["status"]
print(f"4. 模切区报工 → {s} {'✅' if s == '糊盒' else '❌ 期望糊盒'}")

# 糊盒区
api("POST", "/api/public/report", {"productionOrderId": oid, "workshop": "糊盒区", "outputQty": 20, "wasteQty": 0, "operator": "王五"})
s = api("GET", f"/api/public/production-order/{oid}")["data"]["status"]
print(f"5. 糊盒区报工 → {s} {'✅' if s == '打包' else '❌ 期望打包'}")

# 打包区
api("POST", "/api/public/report", {"productionOrderId": oid, "workshop": "打包区", "outputQty": 20, "wasteQty": 0, "operator": "赵六"})
s = api("GET", f"/api/public/production-order/{oid}")["data"]["status"]
print(f"6. 打包区报工 → {s} {'✅' if s == '质检' else '❌ 期望质检'}")

# 质检区 (total output should now be >= 100, so it goes to 已入库)
api("POST", "/api/public/report", {"productionOrderId": oid, "workshop": "质检区", "outputQty": 20, "wasteQty": 0, "operator": "钱七"})
s = api("GET", f"/api/public/production-order/{oid}")["data"]["status"]
print(f"7. 质检区报工(达标) → {s} {'✅' if s == '已入库' else '❌ 期望已入库'}")

print("\n全部测试完成!")
