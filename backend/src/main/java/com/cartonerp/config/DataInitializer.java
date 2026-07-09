package com.cartonerp.config;

import com.cartonerp.entity.*;
import com.cartonerp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private UserRepository userRepo;
    @Autowired private CustomerRepository customerRepo;
    @Autowired private SupplierRepository supplierRepo;
    @Autowired private InventoryRepository inventoryRepo;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepo.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setDisplayName("管理员");
            admin.setRole("admin");
            userRepo.save(admin);
        }

        if (customerRepo.count() == 0) {
            customerRepo.save(makeCustomer("某某食品有限公司", "张经理", "13800001001", "zhang@food.com", "某市工业区1号"));
            customerRepo.save(makeCustomer("某某电子科技有限公司", "李经理", "13800001002", "li@elec.com", "某市高新区2号"));
            customerRepo.save(makeCustomer("某某日用品有限公司", "王经理", "13800001003", "wang@daily.com", "某市经开区3号"));
        }

        if (supplierRepo.count() == 0) {
            supplierRepo.save(makeSupplier("某某纸业有限公司", "赵经理", "13900002001", "纸板", "某市造纸工业园"));
            supplierRepo.save(makeSupplier("某某辅料供应公司", "钱经理", "13900002002", "辅料", "某市化工园区"));
            supplierRepo.save(makeSupplier("某某包装材料厂", "孙经理", "13900002003", "纸板", "某市经济开发区"));
        }

        if (inventoryRepo.count() == 0) {
            inventoryRepo.save(makeInventory("纸板", "三层瓦楞纸板", "1.2m×2.4m", 5000, "张", "A-01", 1000));
            inventoryRepo.save(makeInventory("纸板", "五层瓦楞纸板", "1.2m×2.4m", 3000, "张", "A-02", 800));
            inventoryRepo.save(makeInventory("辅料", "水性油墨(黑色)", "20kg/桶", 200, "kg", "B-01", 50));
            inventoryRepo.save(makeInventory("辅料", "玉米淀粉胶", "25kg/袋", 500, "kg", "B-02", 100));
            inventoryRepo.save(makeInventory("成品", "标准快递纸箱", "40×30×20cm", 1200, "个", "C-01", 300));
            inventoryRepo.save(makeInventory("成品", "食品包装箱", "50×35×25cm", 800, "个", "C-02", 200));
        }
    }

    private Customer makeCustomer(String name, String contact, String phone, String email, String address) {
        Customer c = new Customer();
        c.setName(name); c.setContact(contact); c.setPhone(phone); c.setEmail(email); c.setAddress(address);
        return c;
    }

    private Supplier makeSupplier(String name, String contact, String phone, String supplyType, String address) {
        Supplier s = new Supplier();
        s.setName(name); s.setContact(contact); s.setPhone(phone); s.setSupplyType(supplyType); s.setAddress(address);
        return s;
    }

    private Inventory makeInventory(String itemType, String itemName, String spec, int qty, String unit, String location, int safetyStock) {
        Inventory i = new Inventory();
        i.setItemType(itemType); i.setItemName(itemName); i.setSpec(spec);
        i.setQty(qty); i.setUnit(unit); i.setWarehouseLocation(location); i.setSafetyStock(safetyStock);
        return i;
    }
}
