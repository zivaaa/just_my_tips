const multer = require('multer')

const CONST = require("../../config/const")
const ImageHandler = require("../tools/ImageHandler")
const RESPONSES = require("../tools/responses");

let imageHandler;

module.exports = function (Image) {
    imageHandler = new ImageHandler(Image);

    function makeSuccessResponseFromImages(result) {
        return {
            ...RESPONSES.SUCCEDED,
            result: (Array.isArray(result)) ? result.map(Image.toDto) : Image.toDto(result)
        }
    }


    Image.toDto = function (img) {
        return {
            id: img.id
        }
    };


    /**
     * get all user images
     * @param {Function(Error, array)} callback
     */
    Image.getUserImages = function (req, res, callback) {
        const userId = req.accessToken.userId;

        // check Math exists
        const matchQuery = {
            userId: userId
        };

        Image.find(matchQuery, (err, match) => {
            if (err) {
                return {...RESPONSES.NOT_FOUND, message: err.message}
            }
            callback(null, makeSuccessResponseFromImages(match));
        });
    };

    Image.getSourceImage = function (req, res, id, callback) {
        const userId = req.accessToken ? req.accessToken.userId : null;

        console.log("getImage => " + id);

        Image.findById(id, (err, match) => {
            if (err) {
                return callback({...RESPONSES.DB_ERROR, message: err.message})
            }

            if (!match) {
                return callback({...RESPONSES.NOT_FOUND, message: "no such image"})
            }

            switch (match.accessType) {
                case CONST.IMAGE_ACCESS_TYPE_EVERYONE:
                    break;
                case CONST.IMAGE_ACCESS_TYPE_PRIVATE:
                    if (userId != match.userId) {
                        return callback(RESPONSES.PERMISSION_DENIED)
                    }
                    break;
                case CONST.IMAGE_ACCESS_TYPE_LISTED:
                    break;
            }

            imageHandler.handleImage(match, res, (errorMessage) => {
                callback({...RESPONSES.NOT_FOUND, message: errorMessage})
            })
        });
    };

    Image.uploadImage = function (req, res, body, callback) {

        const userId = req.accessToken.userId;
        console.log("uploadImage by " + userId);
        const uploader = multer({dest: CONST.UPLOADS_TEMP_PATH}).single("userfile");

        uploader(req, res, function (err) {
            if (err) {
                return callback({...RESPONSES.FILE_ERROR, message: err.message})
            }
            var file = req.file;

            if (!file) {
                return callback({...RESPONSES.FILE_ERROR, message: "file not found, do u know what to send?"})
            }

            imageHandler.handleNewImage(userId.toString(), file, (err, img) => {
                if (err) {
                    return callback({...RESPONSES.DB_ERROR, message: err.message})
                }

                callback(null, makeSuccessResponseFromImages(img))

            })
        });
    };

    Image.deleteImage = function (req, res, id, callback) {
        const userId = req.accessToken.userId;
        console.log("delete image " + id);
        Image.findById(id, (err, match) => {
            if (err) {
                return callback({...RESPONSES.DB_ERROR, message: err.message})
            }

            if (!match) {
                return callback({...RESPONSES.NOT_FOUND, message: "no such image"})
            }

            if (userId != match.userId) {
                return callback(RESPONSES.PERMISSION_DENIED)
            }

            imageHandler.clearImageFiles(match);

            Image.deleteAll({
                id : match.id
            }, (err) => {
                if (err) {
                    return callback({...RESPONSES.DB_ERROR, message: err.message})
                }

                callback(null, RESPONSES.SUCCEDED)
            })
        });
    }
};
