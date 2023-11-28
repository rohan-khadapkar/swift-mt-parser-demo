package com.example.swiftmtparserdemo;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.prowidesoftware.swift.model.MtSwiftMessage;
import com.prowidesoftware.swift.model.mt.mt1xx.MT103;
import com.prowidesoftware.swift.utils.Lib;

public class MessageParsingDemo {
    public static void main(String[] args) throws IOException {
        String field33BCurrencyCode = "";
        String field32ACurrencyCode = "";

        Double field36 = 0.0;
        Double field33B = 0.0;
        Double field32A = 0.0;
        Double field71G = 0.0;

        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resourcePatternResolver.getResources("classpath:/docs/**");
        for (Resource resource : resources) {
            System.out.println("Parsing file " + resource.getFilename());
            MtSwiftMessage msg = MtSwiftMessage.parse(Lib.readResource("docs/" + resource.getFilename(), null));
            System.out.println("Message ID: " + msg.getMtId());

            if (msg.isType(103)) {
                MT103 mt = new MT103(msg);
                if (mt.getField20().getValue() != null) {
                    System.out.println("Field 20: " + mt.getField20().getValue());

                    String field20Value = mt.getField20().getValue();

                    // This is based on Swift X Character set
                    // https://www2.swift.com/knowledgecentre/publications/usgi_20230720/2.0?topic=con_31519.htm
                    String xCharacterSet = "[a-zA-Z0-9/\\-?:().,'+\\r\\n\\s]";

                    // Field 20 required format/content should be 16x
                    // https://www2.swift.com/knowledgecentre/publications/us1m_20230720/2.0?topic=con_sfld_MaFrGAQQEe2AI4OK6vBjrg_2125058700fld.htm
                    String field20Pattern = "^" + xCharacterSet + "{16}$";

                    Pattern regex = Pattern.compile(field20Pattern);
                    Matcher matcher = regex.matcher(field20Value);
                    if (matcher.matches() == true) {
                        System.out.println("Content of Field 20 is valid");
                    } else {
                        System.out.println("Content of Field 20 is invalid");
                    }

                    // Network validation
                    // Link for network validation rules for MT 103
                    // https://www2.swift.com/knowledgecentre/publications/us1m_20230720/2.0?topic=mt103-network-rules.htm
                    // The following example is for C1
                    if (mt.getField33B().getValue() != null) {
                        field33BCurrencyCode = mt.getField33B().getCurrency();
                    }
                    if (mt.getField32A().getValue() != null) {
                        field32ACurrencyCode = mt.getField32A().getCurrency();
                    }
                    if (!field32ACurrencyCode.equals(field33BCurrencyCode)) {
                        if (mt.getField36() == null || mt.getField36().getValue() == null) {
                            System.out.println("Network validation rule C1 passed");
                        } else {
                            System.out.println("Network validation rule C1 failed");
                        }
                    }

                    // Usage rules validation
                    // https://www2.swift.com/knowledgecentre/publications/us1m_20230720/2.0?topic=mt103-usage-rules-1.htm
                    if (mt.getField33B() != null && mt.getField33B().getAmount() != null) {
                        String strValue = mt.getField33B().getAmount();
                        strValue = strValue.replace(',', '.');
                        field33B = Double.parseDouble(strValue);
                    }
                    if (mt.getField36() != null && mt.getField36().getValue() != null) {
                        String strValue = mt.getField36().getValue();
                        strValue = strValue.replace(',', '.');
                        field36 = Double.parseDouble(strValue);
                    }
                    if (mt.getField71G() != null && mt.getField71G().getValue() != null) {
                        String strValue = mt.getField71G().getValue();
                        strValue = strValue.replace(',', '.');
                        field71G = Double.parseDouble(strValue);
                    }
                    if (mt.getField32A() != null && mt.getField32A().getValue() != null) {
                        String strValue = mt.getField32A().getAmount();
                        strValue = strValue.replace(',', '.');
                        field32A = Double.parseDouble(strValue);
                    }

                    Double epsilon = 0.000001d;
                    Double amountSum = field33B + field36 + field71G;
                    if (Math.abs(amountSum - field32A) < epsilon) {
                        System.out.println("Usage Rules for Amount Related Fields satisfied");
                    } else {
                        System.out.println("Usage Rules for Amount Related Fields not satisfied");
                    }

                } else {
                    System.out.println("Mandatory field 20 missing");
                }
            }

            break;
        }
    }
}
