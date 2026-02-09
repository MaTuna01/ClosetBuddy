package io.codebuddy.gatewayservice.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.*;

public class MutableHttpServletRequest extends HttpServletRequestWrapper {

    private final Map<String, String> customHeaders = new HashMap<>();
    // 무시할 헤더의 리스트를 중복방지를 적용하여 관리하기 위해 set 활용
    // 객체는 불변객체로, 삭제가 불가능하기 때문에 Authorization의 전파를 막기 위해서 전파하지 않을 헤더
    // 목록을 사용
    private final Set<String> removedHeaders = new HashSet<>();

    public MutableHttpServletRequest(HttpServletRequest request) {
        super(request);
    }

    // 커스텀 헤더 주입 메서드
    public void putHeader(String name, String value) {
        this.customHeaders.put(name, value);
    }

    // Authorization헤더를 제거하기위한 헤더 제거 메서드
    public void removeHeader(String name) {
        this.customHeaders.remove(name);
    }

    @Override
    public String getHeader(String name) {
        // 제거된 헤더는 null 반환 -> 네트워크 전파시 Authorization 헤더가 복사되지 않음
        if (removedHeaders.contains(name.toLowerCase())) {
            return null;
        }
        String headerValue = customHeaders.get(name);
        if(headerValue != null) {
            return headerValue;
        }
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        Set<String> names = new HashSet<>(customHeaders.keySet());
        Enumeration<String> base = super.getHeaderNames();
        while (base.hasMoreElements()) {
            String headerName = base.nextElement();
            // 제거된 헤더는 목록에서 제외 즉, gateway내에서만 Authorization 헤더가 남아있고 이외 서비스 전파시 제거됨
            if (!removedHeaders.contains(headerName.toLowerCase())) {
                names.add(headerName);
            }
        }
        return Collections.enumeration(names);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        // 제거된 헤더는 빈 목록 반환
        if (removedHeaders.contains(name.toLowerCase())) {
            return Collections.emptyEnumeration();
        }
        if (customHeaders.containsKey(name)) {
            return Collections.enumeration(List.of(customHeaders.get(name)));
        }
        return super.getHeaders(name);
    }
}
