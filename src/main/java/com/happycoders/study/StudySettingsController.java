package com.happycoders.study;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.happycoders.account.CurrentAccount;
import com.happycoders.domain.Account;
import com.happycoders.domain.Study;
import com.happycoders.domain.Tag;
import com.happycoders.domain.Zone;
import com.happycoders.study.form.StudyDescriptionForm;
import com.happycoders.tag.TagForm;
import com.happycoders.tag.TagRepository;
import com.happycoders.tag.TagService;
import com.happycoders.zone.ZoneForm;
import com.happycoders.zone.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/study/{path}/settings")
@RequiredArgsConstructor
@Controller
public class StudySettingsController {

    private final StudyService studyService;

    private final ModelMapper modelMapper;

    private final TagService tagService;

    private final TagRepository tagRepository;

    private final ZoneRepository zoneRepository;

    private final ObjectMapper objectMapper;

    @GetMapping("/description")
    public String viewStudySetting(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute(modelMapper.map(study, StudyDescriptionForm.class));
        return "study/settings/description";
    }

    @PostMapping("/description")
    public String updateStudyInfo(@CurrentAccount Account account, @PathVariable String path,
                                  @Valid StudyDescriptionForm studyDescriptionForm, Errors errors,
                                  Model model, RedirectAttributes attributes) {
        Study study = studyService.getStudyToUpdate(account, path);

        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(study);
            // studyDescriptionForm과 error는 model이 자동적으로 담아주기때문에 다시 보낼 필요가 없다. 하지만, account와 study는 다시 넣어줘야하므로 model에 넣어서 보낸다.
            return "study/settings/description";
        }

        studyService.updateStudyDescription(study, studyDescriptionForm);
        attributes.addFlashAttribute("message", "스터디 소개를 수정했습니다.");
        return "redirect:/study/" + getPath(path) + "/settings/description";
    }

    @GetMapping ("/banner")
    public String studyImageForm (@CurrentAccount Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(study);
        return "study/settings/banner";
    }

    @PostMapping ("/banner")
    public String studyImageSubmit (@CurrentAccount Account account, @PathVariable String path, String image, RedirectAttributes attributes) {
        Study study = studyService.getStudyToUpdate(account, path);
        studyService.updateStudyImage(study, image);
        attributes.addFlashAttribute("message", "스터디 이미지를 수정했습니다.");
        return "redirect:/study/" + getPath(path) + "/settings/banner";
    }

    @PostMapping ("/banner/enable")
    public String enableStudyBanner (@CurrentAccount Account account, @PathVariable String path) {
        Study study = studyService.getStudyToUpdate(account, path);
        studyService.enableStudyBanner(study);
        return "redirect:/study/" + getPath(path) + "/settings/banner";
    }

    @PostMapping ("/banner/disable")
    public String disableStudyBanner (@CurrentAccount Account account, @PathVariable String path) {
        Study study = studyService.getStudyToUpdate(account, path);
        studyService.disableStudyBanner(study);
        return "redirect:/study/" + getPath(path) + "/settings/banner";
    }

    @GetMapping ("/tags")
    public String studyTagForm (@CurrentAccount Account account, @PathVariable String path, Model model) throws JsonProcessingException {
        Study study = studyService.getStudyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(study);

        model.addAttribute("tags", study.getTags().stream().map(Tag::getTitle).collect(Collectors.toList()));

        List<String> allTagTitles = tagRepository.findAll().stream()
                .map(Tag::getTitle).collect(Collectors.toList());

        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTagTitles));
        return "study/settings/tags";
    }

    @PostMapping ("/tags/add")
    @ResponseBody
    public ResponseEntity addTag (@CurrentAccount Account account, @PathVariable String path, @RequestBody TagForm tagForm) {
        Study study = studyService.getStudyToUpdateTag(account, path);
        Tag tag = tagService.findOrCreate(tagForm.getTagTitle());
        studyService.addTag (study, tag);
        return ResponseEntity.ok().build();
    }

    @PostMapping ("/tags/remove")
    @ResponseBody
    public ResponseEntity removeTag (@CurrentAccount Account account, @PathVariable String path, @RequestBody TagForm tagForm) {
        Study study = studyService.getStudyToUpdateTag(account, path);
        Tag tag = tagService.findOrCreate(tagForm.getTagTitle());
        if (tag == null) {
            return ResponseEntity.badRequest().build();
        }
        studyService.removeTag(study, tag);
        return ResponseEntity.ok().build();
    }

    @GetMapping ("/zones")
    public String studyZonesForm (@CurrentAccount Account account, @PathVariable String path, Model model) throws JsonProcessingException {
        Study study = studyService.getStudyToUpdate(account, path);

        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute("zones", study.getZones().stream()
                .map(Zone::toString).collect(Collectors.toList()));

        List<String> allZones = zoneRepository.findAll().stream().map(Zone::toString).collect(Collectors.toList());

        model.addAttribute("whitelist", objectMapper.writeValueAsString(allZones));
        return "study/settings/zones";
    }

    @PostMapping ("/zones/add")
    public ResponseEntity addZone (@CurrentAccount Account account, @PathVariable String path, @RequestBody ZoneForm zoneForm) {
        Study study = studyService.getStudyToUpdateZone(account, path);
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if (zone == null)
            return ResponseEntity.badRequest().build();
        studyService.addZone(study, zone);
        return ResponseEntity.ok().build();
    }

    @PostMapping ("/zones/remove")
    public ResponseEntity removeZone (@CurrentAccount Account account, @PathVariable String path, @RequestBody ZoneForm zoneForm) {
        Study study = studyService.getStudyToUpdateZone(account, path);
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());

        if (zone == null)
            return ResponseEntity.badRequest().build();

        studyService.removeZone (study, zone);
        return ResponseEntity.ok().build();
    }

    @GetMapping ("/study")
    public String studySettingForm (@CurrentAccount Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(study);
        return "study/settings/study";
    }

    @PostMapping ("/study/publish")
    public String publishStudy (@CurrentAccount Account account, @PathVariable String path, RedirectAttributes attributes) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        studyService.publish(study);
        attributes.addFlashAttribute("message", "스터디를 공개했습니다.");
        return "redirect:/study" + getPath(path) + "/settings/study";
    }

    @PostMapping ("/study/close")
    public String closeStudy (@CurrentAccount Account account, @PathVariable String path, RedirectAttributes attributes) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        studyService.close(study);
        attributes.addFlashAttribute("message", "스터디를 종료했습니다.");
        return "redirect:/study" + getPath(path) + "/settings/study";
    }



    private String getPath(String path) {
        return URLEncoder.encode(path, StandardCharsets.UTF_8);
    }
}











