package com.xbanchon.processingservice.service;

import com.xbanchon.processingservice.event.ProcessingInstructions;
import lombok.extern.slf4j.Slf4j;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class ImageProcessingEngine {

    public void processImage(String inputFilePath, String outputFilePath, ProcessingInstructions instructions)
            throws IOException, InterruptedException, IM4JavaException {

        IMOperation op = new IMOperation();

        op.addImage(inputFilePath);

        if (instructions != null) {
            // Resize (e.g., {"resize": "800x800"})
            if (instructions.resizeWidth() != null && instructions.resizeWidth() > 0) {
                op.resize(instructions.resizeWidth());
            }

            // Crop (e.g., {"crop": "500x500+10+10"})
//            if (instructions.containsKey("crop")) {
//                op.crop(Integer.valueOf(instructions.get("crop").toString())); // TODO: check docs for this operation
//            }
//
//            // Mirror (Flip horizontally)
//            if (instructions.containsKey("mirror") && (Boolean) instructions.get("mirror")) {
//                op.flop();
//            }
//
//            // Artistic Filters
//            if (instructions.containsKey("filter")) {
//                String filter = instructions.get("filter").toString().toLowerCase();
//                switch (filter) {
//                    case "grayscale":
//                        op.colorspace("gray");
//                        break;
//                    case "sepia":
//                        op.sepiaTone(80.0); // 80% is the standard IM sepia intensity
//                        break;
//                }
//            }
//
//            // Compression / Quality (e.g., {"quality": 75})
//            if (instructions.containsKey("quality")) {
//                op.quality(Double.parseDouble(instructions.get("quality").toString()));
//            }
        }

        op.addImage(outputFilePath);

        ConvertCmd cmd = new ConvertCmd();
        cmd.run(op);
        log.info("Processing complete. Saved to: {}", outputFilePath);
    }
}