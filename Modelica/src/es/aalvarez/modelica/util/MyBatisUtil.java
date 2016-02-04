package es.aalvarez.modelica.util;

import java.io.IOException;
import java.io.Reader;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;




public class MyBatisUtil{
    
    private static SqlSessionFactory factory;

         
    private MyBatisUtil() {
    }

    static
    {
     Reader reader = null;
     try {
      reader = Resources.getResourceAsReader("mybatis-config.xml");
     } catch (IOException e) {
      throw new RuntimeException(e.getMessage());
     }
     factory = new SqlSessionFactoryBuilder().build(reader);
    }

    public static SqlSessionFactory getSqlSessionFactory()
    {
    	//StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        //System.out.println("Llamada a SqlSessionFactory, hilo: " + stackTraceElements[0] +" desde la clase: "+stackTraceElements[2]) ;
     return factory;
    }
}

