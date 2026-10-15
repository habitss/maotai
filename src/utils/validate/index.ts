// validationUtils.ts

export const validateRules = {
  mobile: /^1[3456789]\d{9}$|^01(?:0|1|[6-9])(?:\d{3}-\d{3,4}-\d{3,4}|\d{4}-\d{2,3}-\d{3,4}|\d{8})$/
}

/**
 * 验证手机号码格式（中国或韩国）
 * @param {string} phoneNumber - 要验证的手机号码
 * @returns {boolean} - 如果手机号码格式正确则返回 true，否则返回 false
 */
export function isValidPhoneNumber(phoneNumber: string): boolean {
  const regex = validateRules.mobile;
  return regex.test(phoneNumber);
}

/**
 * 验证电子邮件地址格式
 * @param {string} email - 要验证的电子邮件地址
 * @returns {boolean} - 如果电子邮件地址格式正确则返回 true，否则返回 false
 */
export function isValidEmail(email: string): boolean {
  const regex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
  return regex.test(email);
}

/**
 * 验证URL格式
 * @param {string} url - 要验证的URL
 * @returns {boolean} - 如果URL格式正确则返回 true，否则返回 false
 */
export function isValidURL(url: string): boolean {
  const regex = /^(http|https):\/\/[^ "]+$/;
  return regex.test(url);
}