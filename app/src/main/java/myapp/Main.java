package myapp;

import com.caoccao.javet.interop.*;
import com.caoccao.javet.interop.converters.*;
import com.caoccao.javet.buddy.interop.proxy.*;
import com.caoccao.javet.values.*;
import com.caoccao.javet.interception.jvm.*;
import com.caoccao.javet.exceptions.*;
import com.caoccao.javet.interop.callback.*;
import com.caoccao.javet.utils.*;
import com.caoccao.javet.values.reference.*;

import java.net.*;
import java.util.*;
import java.util.regex.*;

public class Main{
  public static V8Runtime runtime;
  public static JavetProxyConverter javetProxyConverter;
  public static JavetJVMInterceptor javetJVMInterceptor;
  public static void test(Cons<String> func){
    func.get("Test");
  }
  public static void main(String[] args) {
    try {
      runtime = V8Host.getV8Instance().createV8Runtime();
      setupEnv();
      runConsole("const T=(extend(Cons,{get(str){Packages.java.lang.System.out.println(str)}}));Packages.java.lang.System.out.println(T)");
    } catch (Exception err) {
      System.out.println(err.toString());
    }
  }
  public static void setupEnv() throws Exception {
    javetProxyConverter = new JavetProxyConverter();
    javetProxyConverter.getConfig()
        .setReflectionObjectFactory(JavetReflectionObjectFactory.getInstance());
    runtime.setConverter(javetProxyConverter);
    javetJVMInterceptor = new JavetJVMInterceptor(runtime);
    javetJVMInterceptor.addCallbackContexts(
        new JavetCallbackContext("extend", JavetCallbackType.DirectCallNoThisAndResult,
            (IJavetDirectCallable.NoThisAndResult<Exception>) (v8Values) -> {
              if (v8Values.length >= 2) {
                Object object = runtime.toObject(v8Values[0]);
                if (object instanceof Class) {
                  Class<?> clazz = (Class<?>) object;
                  V8ValueObject v8ValueObject = V8ValueUtils.asV8ValueObject(v8Values, 1);
                  if (v8ValueObject != null) {
                    Class<?> childClass =
                        JavetReflectionObjectFactory.getInstance().extend(clazz, v8ValueObject);
                    return runtime.toV8Value(childClass);
                  }
                }
              }
              return runtime.createV8ValueUndefined();
            }));
    javetJVMInterceptor.register(runtime.getGlobalObject());
    runtime.getExecutor("const extend=javet.extend;const Packages=javet.package;const Cons=Packages.myapp.Cons;").executeVoid();
  }

  public static void runConsole(String text) {
    try {
      V8Value v = runtime.getExecutor(text).setResourceName("console.js").execute();
    } catch (Throwable t) {
      System.out.println(t.toString());
    }
  }
}
