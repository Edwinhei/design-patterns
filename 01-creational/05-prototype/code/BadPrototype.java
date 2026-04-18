/**
 * ============================================================
 * 🚫 土办法示例：不用原型模式时的"手动复制字段"之苦
 * ============================================================
 *
 * 场景：一份简历模板要投多家公司，每家略作调整
 *   土办法：每次都 new 新对象 + 手动复制每个字段
 *
 * 运行方式：
 *   java BadPrototype.java
 */

import java.util.ArrayList;
import java.util.List;

public class BadPrototype {

    public static void main(String[] args) {
        // 准备模板简历
        Resume template = new Resume();
        template.setName("张三");
        template.setEmail("zhang3@example.com");
        template.setSkills(new ArrayList<>(List.of("Java", "Spring", "MySQL")));
        template.setProjects(new ArrayList<>(List.of("电商系统", "实时风控")));

        System.out.println("=== 土办法：每次手动复制字段 ===\n");

        // 投阿里 —— 手动复制一份
        Resume aliResume = copyManually(template);
        aliResume.getSkills().add(0, "大数据");   // 阿里强调大数据，放最前
        System.out.println("阿里版:  " + aliResume);

        // 投字节 —— 又手动复制一份
        Resume byteResume = copyManually(template);
        byteResume.getProjects().add(0, "抖音推荐算法");
        System.out.println("字节版:  " + byteResume);

        // ---- 问题暴露 ----
        System.out.println("\n⚠️  问题暴露：");
        System.out.println("1. 每个字段都要手动复制 —— 漏抄一个就是 bug");
        System.out.println("2. Resume 加字段 → 所有 copyManually 调用都要改");
        System.out.println("3. 深拷贝引用类型（List/Map）代码繁琐");
        System.out.println("4. 业务代码里全是'字段复制'的噪音");

        System.out.println("\n👉 下一步：去看 PrototypeDemo.java 三种克隆方式对比");
    }

    /**
     * 土办法：手动把 template 的每个字段复制到新对象
     * 🙁 字段多了这里就是地狱
     */
    static Resume copyManually(Resume template) {
        Resume r = new Resume();
        r.setName(template.getName());
        r.setEmail(template.getEmail());
        // 注意 List 字段必须新建一个，否则两个 Resume 会共享同一个 List
        r.setSkills(new ArrayList<>(template.getSkills()));
        r.setProjects(new ArrayList<>(template.getProjects()));
        return r;
    }
}

// ----------------------------------------------------------------
// 简历类：JavaBean 风格
// ----------------------------------------------------------------
class Resume {
    private String name;
    private String email;
    private List<String> skills = new ArrayList<>();
    private List<String> projects = new ArrayList<>();

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }

    public List<String> getProjects() { return projects; }
    public void setProjects(List<String> projects) { this.projects = projects; }

    @Override
    public String toString() {
        return "📄 " + name + " | 技能" + skills + " | 项目" + projects;
    }
}
