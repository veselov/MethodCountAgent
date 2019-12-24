package org.vps.pa;

import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;

import java.io.ByteArrayInputStream;
import java.io.PrintStream;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

public class MethodCountTransformer implements ClassFileTransformer {

    Collection<Pattern> patterns;

    private final ClassPool pool = new ClassPool(true);


    public MethodCountTransformer(Collection<Pattern> patterns) {
        this.patterns = new ArrayList<>(patterns);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {

        /*
        System.out.println("*** MCT : class "+className+", CBR="+
                (classBeingRedefined==null?"NULL":classBeingRedefined.getName())+
                "; CFB="+classfileBuffer.length+" bytes");
                */

        boolean _do = false;

        for (Pattern r : patterns) {

            if (r.matcher(className).matches()) {
                _do = true;
                break;
            }

        }

        PrintStream log = MethodCountAgent.log;

        if (!_do) {
            log.println("Skipping class "+className);
            return null;
        }

        try {

            CtClass klass =
                    pool.makeClass(new ByteArrayInputStream(classfileBuffer));

            CtConstructor [] ccs = klass.getDeclaredConstructors();
            if (ccs != null) {
                for (CtConstructor cc : ccs) {
                    instrument(className, cc);
                }
            }

            CtMethod[] mms = klass.getDeclaredMethods();
            if (mms != null) {
                for (CtMethod cm : mms) {
                    instrument(className, cm);
                }
            }

            log.println("Instrumented class "+className);

            return klass.toBytecode();

        } catch (Throwable t) {
            log.println("Failed to instrument class "+className);
            t.printStackTrace(log);
            return null;
        }

    }

    private void instrument(String prefix, CtBehavior b) throws Exception {

        if (b.isEmpty()) { return; }

        MethodInfo mi = new MethodInfo(prefix + '.' + b.getName()+
                b.getSignature(), b instanceof CtConstructor);

        int no;

        synchronized (MethodCountAgent.methodInfos) {
            no = MethodCountAgent.methodInfos.size();
            MethodCountAgent.methodInfos.add(mi);
        }

        b.insertBefore("((org.vps.pa.MethodInfo)org.vps.pa.MethodCountAgent.methodInfos.get("+no+
                ")).counter.incrementAndGet();");

    }

}
