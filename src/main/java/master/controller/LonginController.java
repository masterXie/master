package master.controller;

import master.controller.base.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LonginController extends BaseController{

    @RequestMapping(value = "/login")
    public String longin(String account,String name){
        System.out.println(account+""+name);
        return "成功";
    }
}
