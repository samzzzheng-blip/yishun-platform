import javassist.*;
import javassist.bytecode.*;
import java.nio.file.*;
import java.util.*;
public class PatchSearch {
 public static void main(String[] a) throws Exception {
  ClassPool live=new ClassPool(true),donor=new ClassPool(true);
  live.appendClassPath(a[0]);live.appendClassPath(a[1]);donor.appendClassPath(a[1]);
  try(java.util.stream.Stream<Path> files=Files.walk(Paths.get(System.getProperty("user.home"),".m2/repository"))){files.filter(p->p.toString().endsWith(".jar")).forEach(p->{try{live.appendClassPath(p.toString());donor.appendClassPath(p.toString());}catch(Exception e){throw new RuntimeException(e);}});}
  String[][] patches={{"dao.RateDao","findPageByFilters","replace"},{"dao.CartoonDao","searchByNumberOrName","add"},{"service.impl.RateServiceImpl","findRatePageByFilters","body"},{"service.impl.CartoonServiceImpl","findCartoonPageByKeywords","body"},{"controller.admin.RateController","queryRateList","body"},{"controller.admin.CartoonController","queryCartoonList","body"}};
  for(String[] patch:patches){
   String name="com.kiss.yishun."+patch[0];CtClass target=live.get(name),source=donor.get(name);
   Map<String,byte[]> preserved=new HashMap<>();
   for(MethodInfo m:target.getClassFile().getMethods())if(!m.getName().equals(patch[1])&&m.getCodeAttribute()!=null)preserved.put(m.getName()+m.getDescriptor(),m.getCodeAttribute().getCode().clone());
   CtMethod method=source.getDeclaredMethod(patch[1]);
   if(patch[2].equals("body"))target.getDeclaredMethod(patch[1]).setBody(method,null);
   else {
    if(patch[2].equals("replace"))target.removeMethod(target.getDeclaredMethod(patch[1]));
    CtMethod added=CtNewMethod.copy(method,target,null);
    for(String attr:new String[]{"RuntimeVisibleAnnotations","RuntimeInvisibleAnnotations","RuntimeVisibleParameterAnnotations","RuntimeInvisibleParameterAnnotations","Signature"}){AttributeInfo info=method.getMethodInfo().getAttribute(attr);if(info!=null)added.getMethodInfo().addAttribute(info.copy(target.getClassFile().getConstPool(),null));}
    target.addMethod(added);
   }
   for(MethodInfo m:target.getClassFile().getMethods()){byte[] before=preserved.get(m.getName()+m.getDescriptor());if(before!=null&&!Arrays.equals(before,m.getCodeAttribute().getCode()))throw new IllegalStateException("Unrelated method changed "+m.getName());}
   if(target.getClassFile().getMajorVersion()!=52)throw new IllegalStateException("Not Java8");
   target.writeFile(a[2]);System.out.println("PATCHED "+name+" ONLY "+patch[1]);
  }
 }
}
