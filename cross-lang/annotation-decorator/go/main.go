/*
============================================================
Go struct tag + reflect · 可运行示例
============================================================

Go 没有注解，也没有装饰器。
它用最朴素的方式做元数据：【结构体字段后面的字符串标签】

运行：
  go run main.go

需要：Go 1.16+
*/

package main

import (
	"encoding/json"
	"fmt"
	"reflect"
	"strings"
)

// ================================================================
// 带标签的结构体
// ================================================================
// 反引号里的字符串就是 "tag"，多个 tag 用空格分隔
// "key:value" 格式，value 用逗号分隔多个值
type User struct {
	ID    int    `json:"id" validate:"required"`
	Name  string `json:"name" validate:"required,min=2,max=50"`
	Email string `json:"email,omitempty" validate:"required,email"`
	Age   int    `json:"age" validate:"min=0,max=150"`
}

// ================================================================
// 自己实现一个极简校验器（读 validate tag）
// ================================================================
func Validate(v interface{}) []string {
	errors := []string{}
	rv := reflect.ValueOf(v)
	rt := reflect.TypeOf(v)

	// 如果是指针，取指向的值
	if rv.Kind() == reflect.Ptr {
		rv = rv.Elem()
		rt = rt.Elem()
	}

	for i := 0; i < rt.NumField(); i++ {
		field := rt.Field(i)
		value := rv.Field(i)

		// 读 validate tag
		tag := field.Tag.Get("validate")
		if tag == "" {
			continue
		}

		rules := strings.Split(tag, ",")
		for _, rule := range rules {
			rule = strings.TrimSpace(rule)

			// 规则：required
			if rule == "required" {
				if value.Kind() == reflect.String && value.String() == "" {
					errors = append(errors, fmt.Sprintf("%s 不能为空", field.Name))
				}
				if value.Kind() == reflect.Int && value.Int() == 0 {
					errors = append(errors, fmt.Sprintf("%s 不能为 0", field.Name))
				}
			}

			// 规则：min=N
			if strings.HasPrefix(rule, "min=") {
				var n int
				fmt.Sscanf(rule, "min=%d", &n)
				if value.Kind() == reflect.String && len(value.String()) < n {
					errors = append(errors, fmt.Sprintf("%s 长度不能小于 %d", field.Name, n))
				}
				if value.Kind() == reflect.Int && value.Int() < int64(n) {
					errors = append(errors, fmt.Sprintf("%s 不能小于 %d", field.Name, n))
				}
			}

			// 规则：max=N
			if strings.HasPrefix(rule, "max=") {
				var n int
				fmt.Sscanf(rule, "max=%d", &n)
				if value.Kind() == reflect.String && len(value.String()) > n {
					errors = append(errors, fmt.Sprintf("%s 长度不能大于 %d", field.Name, n))
				}
				if value.Kind() == reflect.Int && value.Int() > int64(n) {
					errors = append(errors, fmt.Sprintf("%s 不能大于 %d", field.Name, n))
				}
			}

			// 规则：email（超级简化）
			if rule == "email" {
				if !strings.Contains(value.String(), "@") {
					errors = append(errors, fmt.Sprintf("%s 必须是邮箱格式", field.Name))
				}
			}
		}
	}

	return errors
}

// ================================================================
// 主程序
// ================================================================
func main() {
	// -------- 测试 JSON 序列化（Go 标准库读 json tag）--------
	fmt.Println("=== 测试 1：JSON 序列化（encoding/json 读 json tag）===")
	user := User{
		ID:    1,
		Name:  "张三",
		Email: "zhangsan@example.com",
		Age:   30,
	}

	data, _ := json.MarshalIndent(user, "", "  ")
	fmt.Println(string(data))
	fmt.Println("\n✅ 字段名按 json tag 输出（id / name / email，而不是 ID / Name / Email）")

	// -------- 测试校验（我们自己实现的 Validate，读 validate tag）--------
	fmt.Println("\n=== 测试 2：数据校验（手写的 Validate，读 validate tag）===")

	validUser := User{ID: 1, Name: "张三", Email: "zs@example.com", Age: 25}
	fmt.Println("有效用户：", validUser)
	errors := Validate(&validUser)
	if len(errors) == 0 {
		fmt.Println("  ✅ 校验通过")
	} else {
		fmt.Println("  ❌ 错误:", errors)
	}

	invalidUser := User{ID: 0, Name: "李", Email: "not-email"}
	fmt.Println("\n无效用户：", invalidUser)
	errors = Validate(&invalidUser)
	if len(errors) > 0 {
		fmt.Println("  ❌ 错误:")
		for _, e := range errors {
			fmt.Println("    -", e)
		}
	}

	// -------- 揭秘：用反射查看 tag --------
	fmt.Println("\n=== 测试 3：用反射查看 tag ===")
	rt := reflect.TypeOf(User{})
	for i := 0; i < rt.NumField(); i++ {
		field := rt.Field(i)
		fmt.Printf("字段 %s:\n", field.Name)
		fmt.Printf("  json tag:     %q\n", field.Tag.Get("json"))
		fmt.Printf("  validate tag: %q\n", field.Tag.Get("validate"))
	}

	fmt.Println("\n✅ Go struct tag 原理小结：")
	fmt.Println("   ① 反引号里的字符串就是 tag（纯字符串，编译器不做任何检查）")
	fmt.Println("   ② 用 reflect 包读取：field.Tag.Get(\"key\") → 字符串值")
	fmt.Println("   ③ 各库自己约定 tag 格式（json / validate / gorm / xml 等）")
	fmt.Println("   ④ 最简单但最灵活 —— 没有新语法，一切都是字符串")
}
