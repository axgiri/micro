package github.axgiri.PurchaseService.Controller;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import github.axgiri.PurchaseService.DTO.PurchaseDTO;
import github.axgiri.PurchaseService.Enum.StatusEnum;
import github.axgiri.PurchaseService.Service.PurchaseService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/purchase")
public class PurchaseController {
    
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(PurchaseController.class);

    private final PurchaseService service;

    @Autowired
    public PurchaseController(PurchaseService service){
        this.service = service;
    }

    @GetMapping("/admin")
    public ResponseEntity<List<PurchaseDTO>> get(){
        logger.info("request to fetch all purchases");
        List<PurchaseDTO> purchases = service.get();
        return ResponseEntity.ok(purchases);
    }

    @GetMapping("/admin/getByStatus")
    public ResponseEntity<List<PurchaseDTO>> getByStatus(@RequestParam StatusEnum status){
        logger.info("request to fetch purchases with status: {}", status);
        List<PurchaseDTO> purchases = service.getByStatus(status);
        return ResponseEntity.ok(purchases);
    }

    @GetMapping("/admin/getByUuid")
    public ResponseEntity<PurchaseDTO> getByUuid(@RequestParam UUID uuid){
        logger.info("request to get purchase with uuid: {}", uuid);
        PurchaseDTO purchase = service.getByUuid(uuid);
        return ResponseEntity.ok(purchase);
    }

    @GetMapping("/public/getByGovId")
    public ResponseEntity<PurchaseDTO> getByGovId(@RequestParam String govId){
        logger.info("request to fetch purchase with gov id: {}", govId);
        PurchaseDTO purchase = service.getPurchase(govId);
        return ResponseEntity.ok(purchase);
    }

    @PostMapping(value = "/public/create", consumes = {"multipart/form-data"})
    public ResponseEntity<PurchaseDTO> create(@RequestPart("purchase") @Valid PurchaseDTO purchaseDTO, @RequestPart("pdf") MultipartFile pdfFile) {
        logger.info("request to create new purchase with PDF");
        PurchaseDTO createdPurchase = service.create(purchaseDTO, pdfFile);
        return ResponseEntity.ok(createdPurchase);
    }

    @DeleteMapping("/public/close")
    public ResponseEntity<String> close(@RequestParam UUID uuid){
        logger.info("request to close purchase with uuid: {}", uuid);
        service.close(uuid);
        return ResponseEntity.ok("your insutance package with uuid: " + uuid + " is closed successfully");
    }

    @GetMapping("/public/document/{uuid}")
    public ResponseEntity<byte[]> getDocument(@PathVariable UUID uuid) {
        try {
            byte[] pdfData = service.getPdfByUuid(uuid);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "document_" + uuid + ".pdf");
            headers.setContentLength(pdfData.length);
            return new ResponseEntity<>(pdfData, headers, HttpStatus.OK);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
