# Go struct tag · 示例

## 🎯 这个示例展示了什么

Go 没有"注解"也没有"装饰器"，它用**最朴素的方式**做元数据：**结构体字段后面的字符串标签**。

这个 demo 展示：
1. JSON 序列化如何读 `json` tag
2. 我们**自己实现一个极简 validator**，读 `validate` tag 做字段校验
3. 用反射查看所有 tag

## 🚀 运行

```bash
go run main.go
```

**需要**：Go 1.16+

## 📋 预期输出

```
=== 测试 1：JSON 序列化（encoding/json 读 json tag）===
{
  "id": 1,
  "name": "张三",
  "email": "zhangsan@example.com",
  "age": 30
}

✅ 字段名按 json tag 输出（id / name / email）

=== 测试 2：数据校验（手写的 Validate，读 validate tag）===
有效用户： {1 张三 zs@example.com 25}
  ✅ 校验通过

无效用户： {0 李 not-email 0}
  ❌ 错误:
    - ID 不能为 0
    - Name 长度不能小于 2
    - Email 必须是邮箱格式

=== 测试 3：用反射查看 tag ===
字段 ID:
  json tag:     "id"
  validate tag: "required"
字段 Name:
  json tag:     "name"
  validate tag: "required,min=2,max=50"
...
```

## 🔍 关键代码解读

### 1. 定义 struct + tag

```go
type User struct {
    ID   int    `json:"id" validate:"required"`
    Name string `json:"name" validate:"required,min=2"`
}
```

**反引号里的字符串就是 tag**。格式完全自由，**只是字符串**。

### 2. 读 tag（反射）

```go
rt := reflect.TypeOf(User{})
field, _ := rt.FieldByName("Name")
jsonTag := field.Tag.Get("json")         // "name"
validateTag := field.Tag.Get("validate") // "required,min=2"
```

Go 的标准库 `reflect` 包提供了读 tag 的 API。

### 3. 标准库使用 tag 的例子

```go
import "encoding/json"

user := User{ID: 1, Name: "张三"}
data, _ := json.Marshal(user)    // 内部调用 reflect，按 json tag 序列化
// 输出: {"id":1,"name":"张三"}
```

## 🎭 Go 风格 vs 其他语言

| 方面 | Java 注解 | Go struct tag |
|------|---------|---------------|
| 语法 | 新关键字 `@interface` | 反引号字符串 |
| 编译期校验 | ✅ 强 | ❌ 无（纯字符串，写错不报错）|
| IDE 支持 | ★★★★★ | ★★（最多高亮）|
| 实现复杂度 | 注解类 + 反射 | 直接字符串 + 反射 |
| 表达能力 | 强（可以有字段/方法）| 弱（只能字符串） |

### Go 的哲学
> **"Simplicity is the ultimate sophistication."**

**用最小的语言特性做最多的事** —— 一个反引号字符串，配合标准库 `reflect`，就实现了所有其他语言要用注解做的事。

## ⚠️ Go tag 的坑

### 坑 1：tag 写错不报错
```go
type User struct {
    Name string `jsn:"name"`   // 🚨 写错了（应该是 json）
}
// 编译器不检查，运行时默默失效
```

### 坑 2：需要记住各库的 tag 格式
```go
`json:"name,omitempty"`           // encoding/json
`gorm:"primaryKey;column:name"`   // gorm
`validate:"required,min=2"`       // validator
`xml:"name,attr"`                 // encoding/xml
`form:"name"`                     // gin
```

每个库用自己的 tag key，格式各不相同。

## 🎯 对比其他语言

- [../java/](../java/) —— Java 注解：强类型元数据
- [../python-fastapi/](../python-fastapi/) —— FastAPI：类型提示 + 默认值
- [../typescript-nestjs/](../typescript-nestjs/) —— NestJS：装饰器 + 元数据

**四种方式都做到了"声明式"效果，Go 的方式最朴素**。

## 🔗 扩展阅读

- [../../../java-notes/16 跨语言辨析](../../../java-notes/16-注解与装饰器跨语言辨析.md)
- Go 标准库 `encoding/json` / `encoding/xml`
- 第三方库 `go-playground/validator`、`gorm.io/gorm`
