package configuration.FreemarkException;

import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import freemarker.core.Environment;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

@Component
public class FreemarkerExceptionHandler implements TemplateExceptionHandler{

	 private static final Logger log = LoggerFactory
	            .getLogger(FreemarkerExceptionHandler.class);
	@Override
	public void handleTemplateException(TemplateException arg0,
			Environment arg1, Writer arg2) throws TemplateException {
		log.warn("[Freemarker Error:"+arg0.getMessage()+"]");
		throw new TemplateException("freemarker error",arg1);
	}

}
