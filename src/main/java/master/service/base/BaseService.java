package master.service.base;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author xielei
 * @description
 * @createDate 2017-03-31 15:38
 */
@Transactional(rollbackFor = {Exception.class})
public class BaseService {

    protected Logger logger = LoggerFactory.getLogger(getClass());

}