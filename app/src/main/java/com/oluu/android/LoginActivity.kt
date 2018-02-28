package com.oluu.android

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.cognitoidentityprovider.*
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler
import com.amazonaws.regions.Regions
import com.amazonaws.services.cognitoidentityprovider.AmazonCognitoIdentityProviderClient
import com.amazonaws.util.Base64
import kotlinx.android.synthetic.main.activity_login.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

//TODO: move these later... shouldn't be here
const val AWS_ACCESS_KEY_ID: String = "AKIAIKOYMIY3YKTMKLHQ"
const val AWS_SECRET_ACCESS_KEY: String = "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"
const val USER_POOL_ID: String = "us-east-1_8jXAyv3iW"
const val CLIENT_ID: String = "6ci3qn66kp1pnjonkdmk19smet"
const val CLIENT_SECRET: String = "1bqm3d6u7akafnaveid24atgcpfkbb7d34ec7a31n7ob7o28s6qf"
const val HMAC_SHA_256: String = "HmacSHA256"

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        // cognito setup
        val credentials: AWSCredentials = object: BasicAWSCredentials(AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY) {}
        val userPool = CognitoUserPool(baseContext, USER_POOL_ID, CLIENT_ID, CLIENT_SECRET, Regions.US_EAST_1)
        val identityProvider = AmazonCognitoIdentityProviderClient(credentials)
        val userAttributes = CognitoUserAttributes()
        /************************************
         *** Cognito Callbacks start here ***
         ************************************/

        // login callback
        val authenticationHandler: AuthenticationHandler = object: AuthenticationHandler {
            override fun onSuccess(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                System.out.println("AuthenticationHandler onSuccess invoked!")
                //TODO: login was successful, cognitoUserSession will contain tokens for the user
                //TODO: figure out where we're storing these and which fields we're storing
            }
            override fun getAuthenticationDetails(authenticationContinuation: AuthenticationContinuation, userId: String) {
                System.out.println("getAuthenticationDetails invoked!")
                //TODO: The API needs user login credentials to continue
                 val authenticationDetails = AuthenticationDetails(userId, passwordText.text.toString(),null)
                //TODO: Pass the user login credentials to the continuation
                 authenticationContinuation.setAuthenticationDetails(authenticationDetails)
                //TODO: Allow the login to continue
                 authenticationContinuation.continueTask()
            }
            override fun getMFACode(continuation: MultiFactorAuthenticationContinuation?) {
                System.out.println("AuthenticationHandler getMFACode invoked!")
                // not doing MFA so do nothing here
            }
            override fun authenticationChallenge(continuation: ChallengeContinuation?) {
                System.out.println("AuthenticationHandler authenticationChallenge invoked!")
                //TODO: look into this
            }
            override fun onFailure(exception: Exception) {
                System.out.println("AuthenticationHandler error occurred")
                System.out.println(exception)
                //TODO: handle login error
            }
        }
        // signup callback
        val signupCallback: SignUpHandler = object: SignUpHandler {
            override fun onSuccess(user: CognitoUser?, userConfirmed: Boolean, cognitoUserCodeDeliveryDetails: CognitoUserCodeDeliveryDetails?) {
                if (!userConfirmed) {
                    System.out.println("user is not confirmed")
                } else {
                    System.out.println("attempt to login the user")
                    // login the user, this should never occur, but just we will handle this anyways
                    user?.getSessionInBackground(authenticationHandler)
                }
            }
            override fun onFailure(exception: Exception) {
                System.out.println("SignupCallback error occurred")
                // if the UsernameExistsException occurs lets attempt to log them in
                if (exception.toString().contains("UsernameExistsException", false)) {
                    val mac = Mac.getInstance(HMAC_SHA_256)
                    val signingKey = SecretKeySpec(CLIENT_SECRET.toByteArray(), HMAC_SHA_256)
                    mac.init(signingKey)
                    mac.update(emailText.text.toString().toByteArray())
                    val rawHmac = mac.doFinal(CLIENT_ID.toByteArray())
                    val secretHash = String(Base64.encode(rawHmac))
                    val user: CognitoUser = object: CognitoUser(userPool, emailText.text.toString(), CLIENT_ID, CLIENT_SECRET, secretHash, identityProvider, applicationContext) {}
                    user.getSessionInBackground(authenticationHandler)
                }
                //TODO: Sign up failed, code check the exception for cause and perform remedial actions.
            }
        }

        /**********************************
         *** Cognito Callbacks end here ***
         **********************************/

        // sign up button retrieves text and invokes the signup callback
        signupBtn.setOnClickListener {
            userAttributes.addAttribute("email", emailText.text.toString())
            userPool.signUpInBackground(emailText.text.toString(), passwordText.text.toString(), userAttributes, null, signupCallback)
        }
    }

}
