import javassist.*;
import javassist.bytecode.*;
import java.nio.file.*;
import java.util.*;

public class PatchSearch {
  public static void main(String[] a) throws Exception {
    ClassPool live = new ClassPool(true), donor = new ClassPool(true);
    live.appendClassPath(a[0]); live.appendClassPath(a[1]); donor.appendClassPath(a[1]);
    try(java.util.stream.Stream<Path> files=Files.walk(Paths.get(System.getProperty("user.home"),".m2/repository"))) {
      files.filter(p->p.toString().endsWith(".jar")).forEach(p->{try {live.appendClassPath(p.toString());donor.appendClassPath(p.toString());}catch(Exception e){throw new RuntimeException(e);}});
    }
    String[] classes={"dao.PreciousDao","service.impl.PreciousServiceImpl","controller.admin.PreciousController"};
    String[] methods={"searchByNumberOrSigner","findPreciousPageByKeywords","queryPreciousList"};
    for(int i=0;i<classes.length;i++) {
      String name="com.kiss.yishun."+classes[i];
      CtClass target=live.get(name), source=donor.get(name);
      Map<String,byte[]> unchanged=new HashMap<>();
      for(MethodInfo m:target.getClassFile().getMethods()) if(!m.getName().equals(methods[i]) && m.getCodeAttribute()!=null) unchanged.put(m.getName()+m.getDescriptor(),m.getCodeAttribute().getCode().clone());
      if(i==0) {
        CtMethod original=source.getDeclaredMethod(methods[i]);
        CtMethod added=CtNewMethod.copy(original,target,null);
        for(String attribute:new String[]{"RuntimeVisibleAnnotations","RuntimeInvisibleAnnotations","RuntimeVisibleParameterAnnotations","RuntimeInvisibleParameterAnnotations","Signature"}) {
          AttributeInfo info=original.getMethodInfo().getAttribute(attribute);
          if(info!=null) added.getMethodInfo().addAttribute(info.copy(target.getClassFile().getConstPool(),null));
        }
        target.addMethod(added);
      }
      else target.getDeclaredMethod(methods[i]).setBody(source.getDeclaredMethod(methods[i]),null);
      for(MethodInfo m:target.getClassFile().getMethods()) {
        byte[] before=unchanged.get(m.getName()+m.getDescriptor());
        if(before!=null && !Arrays.equals(before,m.getCodeAttribute().getCode())) throw new IllegalStateException("Unrelated method changed: "+m.getName());
      }
      if(target.getClassFile().getMajorVersion()!=52) throw new IllegalStateException("Not Java 8");
      target.writeFile(a[2]);
      System.out.println("PATCHED "+name+" ONLY "+methods[i]+"; other method bytecode preserved");
    }
  }
}
