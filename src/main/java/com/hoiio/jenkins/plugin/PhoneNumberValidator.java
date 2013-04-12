/*
Copyright (C) 2013 Hoiio Pte Ltd (http://www.hoiio.com)

Permission is hereby granted, free of charge, to any person
obtaining a copy of this software and associated documentation
files (the "Software"), to deal in the Software without
restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following
conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.
*/

package com.hoiio.jenkins.plugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneNumberValidator {

    private static final int PHONE_NUMBER_MIN_LENGTH = 5;
    private static final int PHONE_NUMBER_MAX_LENGTH = 15;

    private PhoneNumberValidator() {
    }

    public static boolean validatePhoneNumber(String phoneNumber) {
        return validatePhoneNumber(phoneNumber, false);
    }

    public static boolean validatePhoneNumber(String phoneNumber,
                                              boolean skipLengthCheck) {
        if (isEmpty(phoneNumber)) {
            return false;
        }
        if (!hasPlusSign(phoneNumber)) {
            return false;
        }
        if (hasInvalidChars(phoneNumber.substring(1, phoneNumber.length()))) {
            return false;
        }

        if (!skipLengthCheck) {
            String trimPhoneNumber = phoneNumber.substring(1);
            if (!validLength(trimPhoneNumber)) {
                return false;
            }
        }

        return true;
    }


    private static boolean isEmpty(String origPhoneNum) {
        if (origPhoneNum == null || origPhoneNum.trim().equals("")) {
            return true;
        }
        return false;
    }

    private static boolean hasPlusSign(String origPhoneNum) {
        if (origPhoneNum.charAt(0) != '+') {
            return false;
        }
        return true;
    }

    public static boolean hasInvalidChars(String phoneNumber) {
        String invalidCharFound = findNonDigitChars(phoneNumber);
        if (invalidCharFound != null) {
            return true;
        }
        return false;
    }

    private static boolean validLength(String phoneNumber) {
        if (phoneNumber == null) {
            return false;
        }
        return (phoneNumber.length() <= PHONE_NUMBER_MAX_LENGTH)
                && (phoneNumber.length() >= PHONE_NUMBER_MIN_LENGTH);
    }

    public static String findNonDigitChars(String str) {
        if (str == null) {
            return null;
        }

        final Pattern p = Pattern.compile("[^0-9]");
        final Matcher m = p.matcher(str);

        boolean found = m.find();
        if (!found) {
            return null;
        } else {
            return m.group();
        }
    }
}