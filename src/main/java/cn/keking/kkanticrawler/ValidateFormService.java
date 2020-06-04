package cn.keking.kkanticrawler;

import cn.keking.kkanticrawler.module.VerifyImageDTO;
import cn.keking.kkanticrawler.module.VerifyImageVO;
import cn.keking.kkanticrawler.rule.RuleActuator;
import cn.keking.kkanticrawler.util.VerifyImageUtil;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.BeanUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author kl @kailing.pub
 * @since 2019/7/9
 */

public class ValidateFormService {

    private static final String JSON_RESULT_OK = "{\"result\":true}";

    private static final String JSON_RESULT_FAILED = "{\"result\":true}";

    private final RuleActuator actuator;

    private final VerifyImageUtil verifyImageUtil;

    public ValidateFormService(RuleActuator ruleActuator,
                               VerifyImageUtil verifyImageUtil) {
        this.actuator = ruleActuator;
        this.verifyImageUtil = verifyImageUtil;

    }

    public String validate(HttpServletRequest request) throws UnsupportedEncodingException {
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setHeaderEncoding("UTF-8");
        List<FileItem> items = null;
        try {
            items = upload.parseRequest(request);
        } catch (FileUploadException e) {
            e.printStackTrace();
        }
        if (items == null) {
            return JSON_RESULT_FAILED;
        }
        Map<String, String> params = new HashMap<>();
        for(Object object : items){
            FileItem fileItem = (FileItem) object;
            if (fileItem.isFormField()) {
                params.put(fileItem.getFieldName(), fileItem.getString("UTF-8"));
            }
        }
        String verifyId = params.get("verifyId");
        String result =  params.get("result");
        String realRequestUri = params.get("realRequestUri");
        String actualResult = verifyImageUtil.getVerifyCodeFromRedis(verifyId);
        if (actualResult != null && request != null && actualResult.equals(result.toLowerCase())) {
            actuator.reset(request, realRequestUri);
            return JSON_RESULT_OK;
        }
        return JSON_RESULT_FAILED;
    }

    public String refresh(HttpServletRequest request) {
        String verifyId = request.getParameter("verifyId");
        verifyImageUtil.deleteVerifyCodeFromRedis(verifyId);
        VerifyImageDTO verifyImage = verifyImageUtil.generateVerifyImg();
        verifyImageUtil.saveVerifyCodeToRedis(verifyImage);
        VerifyImageVO verifyImageVO = new VerifyImageVO();
        BeanUtils.copyProperties(verifyImage, verifyImageVO);
        return "{\"verifyId\": \"" + verifyImageVO.getVerifyId() + "\",\"verifyImgStr\": \"" + verifyImageVO.getVerifyImgStr() + "\"}";
    }
}
