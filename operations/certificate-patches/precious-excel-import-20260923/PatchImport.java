import javassist.*;
import javassist.bytecode.*;
import java.nio.file.*;
import java.util.*;
public class PatchImport {
 public static void main(String[] a) throws Exception {
  ClassPool live=new ClassPool(true), donor=new ClassPool(true);
  live.appendClassPath(a[0]);live.appendClassPath(a[1]);donor.appendClassPath(a[1]);
  try(java.util.stream.Stream<Path> files=Files.walk(Paths.get(System.getProperty("user.home"),".m2/repository"))){files.filter(p->p.toString().endsWith(".jar")).forEach(p->{try{live.appendClassPath(p.toString());donor.appendClassPath(p.toString());}catch(Exception e){throw new RuntimeException(e);}});}
  CtClass target=live.get("com.kiss.yishun.controller.admin.PreciousController"),source=donor.get(target.getName());
  Map<String,byte[]> unchanged=new HashMap<>();
  for(MethodInfo m:target.getClassFile().getMethods())if(!m.getName().equals("readExcel")&&m.getCodeAttribute()!=null)unchanged.put(m.getName()+m.getDescriptor(),m.getCodeAttribute().getCode().clone());
  CtField field=new CtField(source.getDeclaredField("preciousImportService"),target);
  AttributeInfo ann=source.getDeclaredField("preciousImportService").getFieldInfo().getAttribute("RuntimeVisibleAnnotations");
  field.getFieldInfo().addAttribute(ann.copy(target.getClassFile().getConstPool(),null));target.addField(field);
  target.removeMethod(target.getDeclaredMethod("readExcel"));
  for(String name:new String[]{"readExcel","previewExcel"}){
   CtMethod original=source.getDeclaredMethod(name),added=CtNewMethod.copy(original,target,null);
   for(String attr:new String[]{"RuntimeVisibleAnnotations","RuntimeVisibleParameterAnnotations","Signature"}){AttributeInfo info=original.getMethodInfo().getAttribute(attr);if(info!=null)added.getMethodInfo().addAttribute(info.copy(target.getClassFile().getConstPool(),null));}
   target.addMethod(added);
  }
  for(MethodInfo m:target.getClassFile().getMethods()){byte[] old=unchanged.get(m.getName()+m.getDescriptor());if(old!=null&&!Arrays.equals(old,m.getCodeAttribute().getCode()))throw new Exception("Unexpected method change: "+m.getName());}
  target.writeFile(a[2]);System.out.println("Only import endpoints and injection added; all unrelated method bytecode preserved.");
 }
}
