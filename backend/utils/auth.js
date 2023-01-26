const jwt = require('jsonwebtoken')

function generateToken(userInfo) {
    if (!userInfo) {
        return null;
    }

    return jwt.sign(userInfo, process.env.JWT_SECRET, {
        expiresIn: '1h'
    })
}

function verifyToken(username, token) {
    return jwt.verify(token, process.env.JWT_SECRET, (error, response) => {
        if (error) {
            return {
                verified: false,
                message: 'invalide token'
            }
        }
        if (response.username !== username) {
            return {
                verified: false,
                message: 'invalid user'
            }
        }
        return {
            verified: true,
            message: 'verified'
        }
    })
    //jwt.decode(token)
}

function verifyTokenNoUser(token) {
    return jwt.verify(token, process.env.JWT_SECRET, (error, response) => {
        if (error) {
            return {
                verified: false,
                message: 'invalide token'
            }
        }
        return {
            verified: true,
            message: 'verified'
        }
    })
}
function getUserFromToken(token) {
    return jwt.verify(token, process.env.JWT_SECRET, async (error, response) => {
        if (error) {
            return {
                verified: false,
                message: 'invalide token'
            }
        }
        return response.username
    })
}



module.exports.generateToken = generateToken;
module.exports.verifyToken = verifyToken;
module.exports.verifyTokenNoUser = verifyTokenNoUser;
module.exports.getUserFromToken = getUserFromToken;