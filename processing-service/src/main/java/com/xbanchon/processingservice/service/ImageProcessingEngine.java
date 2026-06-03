package com.xbanchon.processingservice.service;

import lombok.extern.slf4j.Slf4j;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
public class ImageProcessingEngine {

    public void processImage(String inputFilePath, String outputFilePath, Map<String, Object> instructions)
            throws IOException, InterruptedException, IM4JavaException {

        IMOperation op = new IMOperation();

        op.addImage(inputFilePath);

        if (instructions != null) {
            // Resize (e.g., {"resize": "800x800"})
            if (instructions.containsKey("resize")) {
                op.resize(Integer.parseInt(instructions.get("resize").toString()));
            }

            // Crop (e.g., {"crop": "500x500+10+10"})
            if (instructions.containsKey("crop")) {
                op.crop(Integer.valueOf(instructions.get("crop").toString())); // TODO: check docs for this operation
            }

            // Mirror (Flip horizontally)
            if (instructions.containsKey("mirror") && (Boolean) instructions.get("mirror")) {
                op.flop();
            }

            // Artistic Filters
            if (instructions.containsKey("filter")) {
                String filter = instructions.get("filter").toString().toLowerCase();
                switch (filter) {
                    case "grayscale":
                        op.colorspace("gray");
                        break;
                    case "sepia":
                        op.sepiaTone(80.0); // 80% is the standard IM sepia intensity
                        break;
                }
            }

            // Compression / Quality (e.g., {"quality": 75})
            if (instructions.containsKey("quality")) {
                op.quality(Double.parseDouble(instructions.get("quality").toString()));
            }
        }

        op.addImage(outputFilePath);

        log.info("Executing ImageMagick command...");
        ConvertCmd cmd = new ConvertCmd();

        // Note: If you are testing this locally on Windows without Docker,
        // you might need to set the search path:
        // cmd.setSearchPath("C:\\Program Files\\ImageMagick-7.X.X-Q16");

        cmd.run(op);
        log.info("ImageMagick processing complete.");
    }
}