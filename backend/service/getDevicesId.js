const AWS = require('aws-sdk');
const util = require('../utils/util');
const auth = require('../utils/auth')
AWS.config.update({
    region: 'eu-central-1',
})

const dynamodb = new AWS.DynamoDB.DocumentClient();
const userTable = 'stm32-users';


const getDevices = async (userToken) => {


    if (!userToken) {
        return util.buildResponse(401, {
            message: 'Please insert the userToken'
        })
    }

    if (await auth.verifyTokenNoUser(userToken).verified == false) {
        return util.buildResponse(402, {
            message: 'Invalid token'
        })
    }

    let username = await auth.getUserFromToken(userToken)

    const params = {
        TableName: userTable,
        Key: {
            username: username
        },
    }
    let data = await dynamodb.get(params).promise().then(response => {
        console.log(response.Item)
        return response.Item;
    }, error => {
        console.error('There is an error getting user: ', error)
    })
    if (data.devices) {
        const response = {
            devices: data.devices,
        }
        return util.buildResponse(200, response)
    } else {
        return util.buildResponse(400, { message: "No data devices for your account have been found." })
    }
}

module.exports.getDevices = getDevices;