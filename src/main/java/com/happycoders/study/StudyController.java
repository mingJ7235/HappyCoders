package com.happycoders.study;

import com.happycoders.account.CurrentAccount;
import com.happycoders.domain.Account;
import com.happycoders.domain.Study;
import com.happycoders.study.form.StudyForm;
import com.happycoders.study.validator.StudyFormValidator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Controller
public class StudyController {

    private final StudyService studyService;

    private final ModelMapper modelMapper;

    private final StudyFormValidator studyFormValidator;

    private final StudyRepository studyRepository;

    @InitBinder("studyForm")
    public void studyFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(studyFormValidator);
    }

    @GetMapping("/new-study")
    public String newStudyForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new StudyForm());
        return "study/form";
    }

    @PostMapping("/new-study")
    public String newStudySubmit(@CurrentAccount Account account, @Valid StudyForm studyForm, Errors errors, Model model) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return "study/form";
        }

        Study newStudy = studyService.createNewStudy(modelMapper.map(studyForm, Study.class), account);
        return "redirect:/study/" + URLEncoder.encode(newStudy.getPath(), StandardCharsets.UTF_8);
    }

    @GetMapping("/study/{path}")
    public String viewStudy(@CurrentAccount Account account, @PathVariable String path, Model model) {
//        Study study = studyRepository.findByPath(path);
        Study study = studyRepository.findByPath(path);
        model.addAttribute(account);
        model.addAttribute(study);
        return "study/view";
    }

    @GetMapping("/study/{path}/members")
    public String viewStudyMembers(@CurrentAccount Account account, @PathVariable String path, Model model) {
//        Study study = studyService.getStudy(path);
        Study study = studyRepository.findByPath(path);
        model.addAttribute(account);
        model.addAttribute(study);
        return "study/members";
    }

    @GetMapping ("/study/{path}/join") //TODO : post 로 변경할것
    public String joinStudy (@CurrentAccount Account account, @PathVariable String path, Model model) {
        Study study = studyService.addMember(path, account);
        model.addAttribute(account);
        model.addAttribute(study);
        return "redirect:/study/" + study.getEncodedPath() + "/members";
    }

    @GetMapping  ("/study/{path}/leave") //TODO : post 로 변경할것
    public String leaveStudy (@CurrentAccount Account account, @PathVariable String path, Model model) {
        Study study = studyService.removeMember(path, account);
        model.addAttribute(account);
        model.addAttribute(study);
        return "redirect:/study/" + study.getEncodedPath() + "/members";
    }

}











