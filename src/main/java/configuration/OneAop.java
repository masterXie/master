
package master.configuration;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(0)
public class OneAop {
	
	@Pointcut("execution(public * master.serviceImpl.HomeServiceImpl.*(..))")
	public void pointCut(){
		System.out.println("切点");
	}
	
	@Before("pointCut()")
	public void before(){
		System.out.println("前置通知");
	}
	
	@AfterReturning("pointCut()")  
    public void doAfter(){  
        System.out.println("后置通知");  
    }  
      
    @After("pointCut()")  
    public void after(){  
        System.out.println("最终通知");  
    }  
      
    @AfterThrowing("pointCut()")  
    public void doAfterThrow(){  
        System.out.println("例外通知");  
    }
}
