const path = require('path');
const sharp = require('sharp');

const CONST = require("../../config/const")
const appDir = path.dirname(require.main.filename);
const mime = require('mime-types')
const fs = require('fs');

class ImageHandler {
    constructor(model) {
        this.Image = model;
    }

    handleImage(imageModel, res, onError) {
        const filePath = path.resolve(CONST.IMAGE_PATH, imageModel.path);
        if (!fs.existsSync(filePath)) {
            return onError("file does not exists!")
        }
        const mimeType = mime.lookup(filePath);
        res.set("Content-Type", mimeType);
        res.set('Cache-Control', 'max-age=2592000');
        res.sendFile(filePath);
    }

    handleNewImage(userId, file, cb) {
        //updateAttributes
        const ext = path.extname(file.originalname);
        const sourceFilePath = path.resolve(file.path);
        const newFileName = file.filename + ext;
        const destFolder = this.makeDestFilePath(userId, "");

        if (!fs.existsSync(destFolder)) {
            fs.mkdir(destFolder);
        }

        const destFilePath = this.makeDestFilePath(userId, newFileName);
        const sharper = sharp(sourceFilePath);
        let dx, dy;

        const onResizeCompleted = (err, data) => {
            if (err) {
                return cb(err.message)
            }

            fs.unlink(sourceFilePath);

            this.Image.create({
                userId: userId,
                path: userId + "/" + newFileName,
                name: newFileName
            }, function (err, img) {
                if (err) {
                    return cb(err.message)
                }

                cb(null, img)
            })
        };

        sharper.metadata()
            .then((meta) => {
                const maxDimen = Math.max(meta.width, meta.height);

                if (maxDimen > CONST.MAX_IMAGE_SIZE) {
                    if (meta.width > meta.height) {
                        dx = CONST.MAX_IMAGE_SIZE
                    } else {
                        dy = CONST.MAX_IMAGE_SIZE
                    }
                }

            }).then(() => {

            if (dx || dy) {
                sharper
                    .resize(dx, dy)
                    .toFile(destFilePath, onResizeCompleted)
            } else {
                sharper.toFile(destFilePath, onResizeCompleted)
            }
        });
    }

    clearImageFiles(image) {
        const absPath = path.resolve(CONST.IMAGE_PATH, image.path);
        if (fs.existsSync(absPath)) {
            fs.unlink(absPath)
        }
    }

    makeDestFilePath(userId, fileName = "") {
        return path.resolve(CONST.IMAGE_PATH, userId, fileName);
    }
}

module.exports = ImageHandler;
