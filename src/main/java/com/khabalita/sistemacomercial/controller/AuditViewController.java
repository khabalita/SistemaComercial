package com.khabalita.sistemacomercial.controller;

import com.khabalita.sistemacomercial.audit.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/ui/audit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditViewController {

    private final AuditService auditService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        var auditPage = auditService.findRecent(PageRequest.of(Math.max(page, 0), 50,
                Sort.by(Sort.Direction.DESC, "createdAt")));
        model.addAttribute("auditLogs", auditPage.getContent());
        model.addAttribute("page", auditPage);
        return "audit/list";
    }
}
