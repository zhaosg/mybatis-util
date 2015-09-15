import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MybatisUtil {
    static class ColumnMeta {
        private String name;
        private String type;
        private String description;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static Logger logger = LoggerFactory.getLogger(MybatisUtil.class);
    static Map<String, String> map;
    static Map<String, String> map1;
    static Map<String, String> map2;
    static String root;
    static String metaSql = "select column_name name,data_type type,column_comment description from information_schema.columns where table_name = ";

    static {
        root = "." + File.separator+"gen" + File.separator;
        map = new HashMap<String, String>();
        map.put("BIGINT", "BIGINT");
        map.put("VARCHAR", "VARCHAR");
        map.put("DECIMAL", "DECIMAL");
        map.put("INT", "INTEGER");
        map.put("LONGTEXT", "LONGVARCHAR");
        map.put("TINYINT", "INTEGER");
        map.put("DATETIME", "TIMESTAMP");

        map1 = new HashMap<String, String>();
        map1.put("BIGINT", "Long");
        map1.put("VARCHAR", "String");
        map1.put("DECIMAL", "java.math.BigDecimal");
        map1.put("INT", "Integer");
        map1.put("LONGTEXT", "String");
        map1.put("TINYINT", "Integer");
        map1.put("DATETIME", "java.util.Date");

        map2 = new HashMap<String, String>();
        map2.put("creator","creator");
        map2.put("createTime","createTime");
        map2.put("updator","creator");
        map2.put("updateTime","createTime");
        map2.put("status","status");
        map2.put("ext1","ext1");
        map2.put("ext2","ext2");
        map2.put("ext3","ext2");
    }

    public static String generate_model_class_ByTable(String config, String name, String table_prefix, String column_prefix, boolean print) {
        String code = null;
        try {
            code = "package com.newglobe.model;\n";
            String clsName = translate_className(name, table_prefix);
            code += "public class "+clsName+" extends BaseModel{\n";
            code += generate_fields_sql_ByTable(config, name, column_prefix, false);
            code += "}";
            if (print) System.out.println(code);
            File file = new File(root + clsName + ".java");
            FileUtils.writeStringToFile(file, code);
        } catch (Exception e) {
            logger.error("", e);
        }
        return code;
    }

    public static String generate_read_sql_ByTable(String config, String name, String table_prefix, String column_prefix, boolean print) {
        String code = null;
        try {
            String clsName = translate_className(name, table_prefix);
            code = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                    "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\" >\n" +
                    "<mapper namespace=\"cn.com.newglobe.dao.read." + clsName + "ReadDao\" >\n";
            code +=generate_resultMap_sql_ByTable(config, name, table_prefix, column_prefix, false)+"\n";
            code +=generate_queryCount_sql_ByTable(config, name, table_prefix, column_prefix, false)+"\n";
            code +=generate_queryList_sql_ByTable(config, name, table_prefix, column_prefix, false)+"\n";
            code +="</mapper>\n";
            if (print) System.out.println(code);
            File file = new File(root + "read" + File.separator + clsName + "Mapper.xml");
            FileUtils.writeStringToFile(file, code);
        } catch (Exception e) {
            logger.error("", e);
        }
        return code;
    }

    public static String generate_queryList_sql_ByTable(String config, String name, String table_prefix, String column_prefix, boolean print) {
        try {
            Connection con = DBUtil.openConnection(config);
            String q = metaSql + "'" + name + "'";
            List<ColumnMeta> metas = DBUtil.queryBeanList(con, q, ColumnMeta.class);
            String clsName = translate_className(name, table_prefix);
            String sql = "\t<select id=\"queryList\" parameterType=\"cn.com.newglobe.model." + clsName + "\" resultMap=\""+clsName+"BaseResultMap\">\n";
            sql += "\t\tselect * from " + name + " where 1=1\n\t\t<trim>\n";
            for (ColumnMeta cloumn : metas) {
                String cname = translate_columnName_to_fieldName(cloumn.getName(), column_prefix);
                String jdbcType = map.get(cloumn.getType().toUpperCase());
                sql += "\t\t\t<if test=\"" + cname + " != null and " + cname + " != ''\"> and " + cloumn.getName() + " = #{" + cname + ",jdbcType=" + jdbcType + "} </if>\n";
            }
            sql += "\t\t\t<if test=\"startRow != null\"> limit #{startRow} </if>\n\t\t\t<if test=\"pageSize != null\">,${pageSize} </if>\n\t\t</trim>\n\t</select>";
            if (print) System.out.println(sql);
            return sql;
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            try {
                DBUtil.closeConnection();
            } catch (Exception e) {
                logger.error("", e);
            }
        }
        return "";
    }

    public static String generate_fields_sql_ByTable(String config, String name, String column_prefix, boolean print) {
        try {
            Connection con = DBUtil.openConnection(config);
            String q = metaSql + "'" + name + "'";
            List<ColumnMeta> metas = DBUtil.queryBeanList(con, q, ColumnMeta.class);
            String fields = "";
            for (ColumnMeta cloumn : metas) {
                String cname = translate_columnName_to_fieldName(cloumn.getName(), column_prefix);
                if(map2.containsKey(cname))
                    continue;
                fields += "\tprivate " + map1.get(cloumn.getType().toUpperCase()) + " " + cname + ";\n";
            }
            if (print) System.out.println(fields);
            return fields;
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            try {
                DBUtil.closeConnection();
            } catch (Exception e) {
                logger.error("", e);
            }
        }
        return null;
    }

    public static String generate_queryCount_sql_ByTable(String config, String name, String table_prefix, String column_prefix, boolean print) {
        try {
            Connection con = DBUtil.openConnection(config);
            String q = metaSql + "'" + name + "'";
            List<ColumnMeta> metas = DBUtil.queryBeanList(con, q, ColumnMeta.class);
            String clsName = translate_className(name, table_prefix);
            String sql1 = "\t<select id=\"queryCount\" parameterType=\"cn.com.newglobe.model." + clsName + "\" resultType=\"java.lang.Integer\">\n";
            sql1 += "\t\tselect count(*) from " + name + " where 1=1\n\t\t<trim>\n";
            for (ColumnMeta cloumn : metas) {
                String cname = translate_columnName_to_fieldName(cloumn.getName(), column_prefix);
                String jdbcType = map.get(cloumn.getType().toUpperCase());
                sql1 += "\t\t\t<if test=\"" + cname + " != null and " + cname + " != ''\"> and " + cloumn.getName() + " = #{" + cname + ",jdbcType=" + jdbcType + "} </if>\n";
            }
            sql1 += "\t\t</trim>\n\t</select>";
            if (print) System.out.println(sql1);
            return sql1;
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            try {
                DBUtil.closeConnection();
            } catch (Exception e) {
                logger.error("", e);
            }
        }
        return null;
    }

    public static String generate_resultMap_sql_ByTable(String config, String name, String table_prefix, String column_prefix, boolean print) {
        try {
            Connection con = DBUtil.openConnection(config);
            String q = metaSql + "'" + name + "'";
            List<ColumnMeta> metas = DBUtil.queryBeanList(con, q, ColumnMeta.class);
            String clsName = translate_className(name, table_prefix);
            String rmap = "\t<resultMap id=\"" + clsName + "BaseResultMap\" type=\"cn.com.newglobe.model." + clsName + "\" >\n";
            for (ColumnMeta cloumn : metas) {
                String cname = translate_columnName_to_fieldName(cloumn.getName(), column_prefix);
                String jdbcType = map.get(cloumn.getType().toUpperCase());
                rmap += "\t\t<result column=\"" + cloumn.getName() + "\" property=\"" + cname + "\" jdbcType=\"" + jdbcType + "\" />\n";
            }
            rmap += "\t</resultMap>";
            if (print) System.out.println(rmap);
            return rmap;
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            try {
                DBUtil.closeConnection();
            } catch (Exception e) {
                logger.error("", e);
            }
        }
        return null;
    }


    public static String generate_write_sql_ByTable(String config, String name, String table_prefix, String column_prefix, boolean print) {
        String code = null;
        try {
            String clsName = translate_className(name, table_prefix);
            code =
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                            "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\" >\n" +
                            "<mapper namespace=\"cn.com.newglobe.dao.write." + clsName + "WriteDao\">\n";
            code += generate_deleteById_sql_ByTable(config, name, false)+"\n";
            code += generate_deleteSelective_sql_ByTable(config, name, table_prefix, column_prefix, false)+"\n";
            code += generate_insert_sql_ByTable(config, name, table_prefix, column_prefix, false)+"\n";
            code += generate_insertSelective_sql_ByTable(config, name, table_prefix, column_prefix, false)+"\n";
            code += generate_updateSelective_sql_ByTable(config, name, table_prefix, column_prefix, false)+"\n";
            code += "</mapper>"+"\n";
            if (print) System.out.println(code);
            File file = new File(root + "write" + File.separator + clsName + "Mapper.xml");
            FileUtils.writeStringToFile(file, code);
        } catch (Exception e) {
            logger.error("", e);
        }
        return code;
    }

    public static ColumnMeta queryPrimaryKeyColumnMeta(Connection con, String name) {
        ColumnMeta pk = null;
        try {
            String q = metaSql + "'" + name + "' and column_key='PRI'";
            List<ColumnMeta> metas = DBUtil.queryBeanList(con, q, ColumnMeta.class);
            if (metas != null && metas.size() > 0) {
                pk = metas.get(0);
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        return pk;
    }

    public static String translate_columnName_to_fieldName(String columnName, String column_prefix) {
        if (StringUtils.isNotEmpty(column_prefix)) {
            return StringUtil.replaceUnderlineAndfirstToUpper(columnName.replace(column_prefix + "_", ""), "_", "");
        }
        return StringUtil.replaceUnderlineAndfirstToUpper(columnName, "_", "");
    }

    public static String translate_className(String name, String table_prefix) {
        String clsName = "";
        if (StringUtils.isNotEmpty(table_prefix)) {
            clsName = name.replace(table_prefix, "");
        }
        clsName = StringUtil.replaceUnderlineAndfirstToUpper(clsName, "_", "");
        return clsName;
    }

    public static String translate_pk_fieldName(Connection con, String name, String column_prefix) {
        String cname = null;
        try {
            ColumnMeta pk = queryPrimaryKeyColumnMeta(con, name);
            if (pk != null) {
                cname = translate_columnName_to_fieldName(pk.getName(), column_prefix);
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        return cname;
    }


    public static String generate_deleteById_sql_ByTable(String config, String name, boolean print) {
        try {
            Connection con = DBUtil.openConnection(config);
            ColumnMeta pk = queryPrimaryKeyColumnMeta(con, name);
            if (pk != null) {
                String sql = "\t<delete id=\"deleteById\" parameterType=\"java.lang.Long\">\n";
                sql += "\t\tdelete from " + name + " where " + pk.getName() + " = #{value,jdbcType=BIGINT}\n\t</delete>";
                if (print) System.out.println(sql);
                return sql;
            }
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            try {
                DBUtil.closeConnection();
            } catch (Exception e) {
                logger.error("", e);
            }
        }
        return "";
    }

    public static String generate_insertSelective_sql_ByTable(String config, String name, String table_prefix, String column_prefix, boolean print) {
        try {
            Connection con = DBUtil.openConnection(config);
            String q = metaSql + "'" + name + "'";
            List<ColumnMeta> metas = DBUtil.queryBeanList(con, q, ColumnMeta.class);
            String clsName = translate_className(name, table_prefix);
            String pkfieldname = translate_pk_fieldName(con, name, column_prefix);
            String sql1 =
                    "\t<insert id=\"insertSelective\" parameterType=\"cn.com.newglobe.model." + clsName + "\" useGeneratedKeys=\"true\" keyProperty=\"" + pkfieldname + "\">\n" +
                            "\t\tinsert into " + name + "\n" +
                            "\t\t<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">\n";
            for (ColumnMeta cloumn : metas) {
                String cname = translate_columnName_to_fieldName(cloumn.getName(), column_prefix);
                sql1 += "\t\t\t<if test=\"" + cname + " != null\"> " + cloumn.getName() + ", </if>\n";
            }
            sql1 += "\t\t</trim>\n" +
                    "\t\t<trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\">\n";
            for (ColumnMeta cloumn : metas) {
                String cname = translate_columnName_to_fieldName(cloumn.getName(), column_prefix);
                String jdbcType = map.get(cloumn.getType().toUpperCase());
                sql1 += "\t\t\t<if test=\"" + cname + " != null\"> #{" + cname + ",jdbcType=" + jdbcType + "}, </if>\n";
            }
            sql1 += "\t\t</trim>\n\t</insert>";
            if (print) System.out.println(sql1);
            return sql1;
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            try {
                DBUtil.closeConnection();
            } catch (Exception e) {
                logger.error("", e);
            }
        }
        return null;
    }

    public static String generate_insert_sql_ByTable(String config, String name, String table_prefix, String column_prefix, boolean print) {
        try {
            Connection con = DBUtil.openConnection(config);
            String q = metaSql + "'" + name + "'";
            List<ColumnMeta> metas = DBUtil.queryBeanList(con, q, ColumnMeta.class);
            String clsName = translate_className(name, table_prefix);
            String pkfieldname = translate_pk_fieldName(con, name, column_prefix);
            String sql1 =
                    "\t<insert id=\"insert\" parameterType=\"cn.com.newglobe.model." + clsName + "\" useGeneratedKeys=\"true\" keyProperty=\"" + pkfieldname + "\">\n" +
                            "\t\tinsert into " + name + "\n" +
                            "\t\t<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">\n\t\t\t";
            for (ColumnMeta cloumn : metas) {
                sql1 += "" + cloumn.getName() + ",";
            }
            sql1 += "\n\t\t</trim>\n" +
                    "\t\t<trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\">\n\t\t\t";
            for (ColumnMeta cloumn : metas) {
                String jdbcType = map.get(cloumn.getType().toUpperCase());
                String cname = translate_columnName_to_fieldName(cloumn.getName(), column_prefix);
                sql1 += "#{" + cname + ",jdbcType=" + jdbcType + "},";
            }
            sql1 += "\n\t\t</trim>\n\t</insert>";
            if (print) System.out.println(sql1);
            return sql1;
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            try {
                DBUtil.closeConnection();
            } catch (Exception e) {
                logger.error("", e);
            }
        }
        return "";
    }

    public static String generate_updateSelective_sql_ByTable(String config, String name, String table_prefix, String column_prefix, boolean print) {
        try {
            Connection con = DBUtil.openConnection(config);
            String q = metaSql + "'" + name + "'";
            List<ColumnMeta> metas = DBUtil.queryBeanList(con, q, ColumnMeta.class);
            String clsName = translate_className(name, table_prefix);
            String sql1 =
                    "\t<update id=\"updateSelective\" parameterType=\"cn.com.newglobe.model." + clsName + "\">\n" +
                            "\t\tupdate " + name + "\n\t\t<set>\n";
            for (ColumnMeta cloumn : metas) {
                String cname = translate_columnName_to_fieldName(cloumn.getName(), column_prefix);
                String jdbcType = map.get(cloumn.getType().toUpperCase());
                sql1 += "\t\t\t<if test=\"" + cname + " != null\">  " + cloumn.getName() + " = #{" + cname + ",jdbcType=" + jdbcType + "}, </if>\n";
            }
            sql1 += "\t\t</set>\n\t</update>";
            if (print) System.out.println(sql1);
            return sql1;
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            try {
                DBUtil.closeConnection();
            } catch (Exception e) {
                logger.error("", e);
            }
        }
        return "";
    }

    public static String generate_deleteSelective_sql_ByTable(String config, String name, String table_prefix, String column_prefix, boolean print) {
        try {
            Connection con = DBUtil.openConnection(config);
            String q = metaSql + "'" + name + "'";
            List<ColumnMeta> metas = DBUtil.queryBeanList(con, q, ColumnMeta.class);
            String clsName = translate_className(name, table_prefix);
            String sql1 =
                    "\t<delete id=\"deleteSelective\" parameterType=\"cn.com.newglobe.model." + clsName + "\">\n" +
                            "\t\tdelete from " + name + " where 1=1\n\t\t<trim>\n";
            for (ColumnMeta cloumn : metas) {
                String cname = translate_columnName_to_fieldName(cloumn.getName(), column_prefix);
                String jdbcType = map.get(cloumn.getType().toUpperCase());
                sql1 += "\t\t\t<if test=\"" + cname + " != null and " + cname + " != ''\"> and " + cloumn.getName() + " = #{" + cname + ",jdbcType=" + jdbcType + "} </if>\n";
            }
            sql1 += "\t\t</trim>\n\t</delete>";
            if (print) System.out.println(sql1);
            return sql1;
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            try {
                DBUtil.closeConnection();
            } catch (Exception e) {
                logger.error("", e);
            }
        }
        return null;
    }

    public static void main(String[] args) {
        MybatisUtil.generate_model_class_ByTable("/application.properties", "t_goods", "t", "goods", true);
        MybatisUtil.generate_read_sql_ByTable("/application.properties", "t_goods", "t", "goods", true);
        MybatisUtil.generate_write_sql_ByTable("/application.properties", "t_goods", "t", "goods", true);
    }

}
