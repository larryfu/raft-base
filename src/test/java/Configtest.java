import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;

import java.util.Iterator;

public class Configtest {

    public static void main(String[] args) {
        try {
            Configurations configs = new Configurations();
            // setDefaultEncoding是个静态方法,用于设置指定类型(class)所有对象的编码方式。
            // 本例中是PropertiesConfiguration,要在PropertiesConfiguration实例创建之前调用。
            FileBasedConfigurationBuilder.setDefaultEncoding(PropertiesConfiguration.class, "UTF-8");
            PropertiesConfiguration propConfig = configs.properties("D:\\home\\cluster.conf");
            Configuration configuration = propConfig.subset("server");
            Iterator<String> iterator = configuration.getKeys();
        do{
            String key = iterator.next();
            System.out.println(key);
        }while (iterator.hasNext());
            System.out.println(configuration.getString("1"));
            //  System.out.println(propConfig.getBoolean("log4j.appender.LOGFILE.Append"));
            //  System.out.println(propConfig.getString("test"));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
