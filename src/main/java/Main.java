import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    public static Logger logger = LoggerFactory.getLogger(MybatisUtil.class);
    public static void main(String[] args) {
        try {
            Options options = new Options();
            options.addOption("tableName", true, "表名");
            options.addOption("tableNames", true, "表名组");
            options.addOption("tablePrefix", true, "表前缀");
            options.addOption("columnPrefix", true, "列前缀");
            options.addOption("clear", false, "删除");
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);
            if(cmd.hasOption("clear"))
                MybatisUtil.clear();
            else{
                String tableName = cmd.getOptionValue("tableName");
                String tablePrefix = cmd.getOptionValue("tablePrefix");
                String columnPrefix = null;
                if(cmd.hasOption("columnPrefix"))
                    columnPrefix= cmd.getOptionValue("columnPrefix");
                String[] tableNames = null;
                if(cmd.hasOption("tableNames"))
                    tableNames= cmd.getOptionValue("tableNames").split(",");
                else
                    tableNames=new String[]{tableName};
                for (int i = 0; i < tableNames.length; i++) {
                    MybatisUtil.generate_model_class_ByTable("/application.properties", tableNames[i], tablePrefix, columnPrefix, false);
                    MybatisUtil.generate_dao_class_ByTable("/application.properties",tableNames[i], tablePrefix, columnPrefix, false);
                    MybatisUtil.generate_service_class_ByTable("/application.properties", tableNames[i], tablePrefix, columnPrefix, false);
                    MybatisUtil.generate_read_sql_ByTable("/application.properties",tableNames[i], tablePrefix, columnPrefix, false);
                    MybatisUtil.generate_write_sql_ByTable("/application.properties",tableNames[i], tablePrefix, columnPrefix, false);
                }
            }
        } catch (Exception e) {
            logger.error("", e);
        }

    }
}
