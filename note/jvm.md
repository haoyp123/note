# JVM

java文件通过词法分析---》语法分析---》语法树--》字节码生成器---》class文件

## The Class File Structure

~~~
ClassFile {
    u4             magic;                
    u2             minor_version;
    u2             major_version;
    u2             constant_pool_count;
    cp_info        constant_pool[constant_pool_count-1];
    u2             access_flags;
    u2             this_class;
    u2             super_class;
    u2             interfaces_count;
    u2             interfaces[interfaces_count];
    u2             fields_count;
    field_info     fields[fields_count];
    u2             methods_count;
    method_info    methods[methods_count];
    u2             attributes_count;
    attribute_info attributes[attributes_count];
}
CAFEBABE
0000
0034
000D
0A0003000A07000B07000C0100063C696E69743E010003282956010004436F646501000F4C696E654E756D6265725461626C6501000A536F7572636546696C6501000B506572736F6E2E6A6176610C00040005010006506572736F6E0100106A6176612F6C616E672F4F626A656374002100020003000000000001000100040005000100060000001D00010001000000052AB70001B1000000010007000000060001

magic 表示class文件的一个识别。 u4 表示16进制文件的前四位。
cafe babe 就表示的是class文件
minor_version 最小版本
major_version 最大版本
constrant_pool_count 常量池数量
~~~





