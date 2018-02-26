//
//  Utils.swift
//  raa-ios-player
//
//  Created by Hamid on 2/2/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation

class Utils {

    private static let _persianNumberFormatter = NumberFormatter()
    private static var persianNumberFormatter: NumberFormatter {
        get {
            _persianNumberFormatter.locale = Locale(identifier: "fa_IR")
            return _persianNumberFormatter
        }
    }
    
    private static let _hoursOnlyDateFormatter = DateFormatter()
    private static var hoursOnlyDateFormatter: DateFormatter {
        get {
            if _hoursOnlyDateFormatter.dateFormat.isEmpty {
                _hoursOnlyDateFormatter.dateFormat = "HH:mm"
            }
            return _hoursOnlyDateFormatter
        }
    }
    
    class func getHourOfDayString(from date: Date?) -> String? {
        guard date != nil else {
            return nil
        }
        return hoursOnlyDateFormatter.string(from: date!)
    }
    
    class func convertToPersianLocaleString(_ str: String?) -> String? {
        if (str == nil) {
            return nil;
        }
        var result: String = str!
        for i in 0...9 {
            result = result.replacingOccurrences(of: String(i), with: persianNumberFormatter.string(from: NSNumber(value: i))!)
        }
        return result
    }

    class func getPersianLocaleDateString(_ date: Date) -> String {
        let components = Calendar.current.dateComponents([.day, .month, .year], from: date)

        var result = ""
        result += Utils.convertToPersianLocaleString(String(describing: components.day!)) ?? ""
        result += " "
        result += Utils.getPersianLocaleMonthName(components.month!)
        result += " "
        result += Utils.convertToPersianLocaleString(String(describing: components.year!)) ?? ""
        
        return result
    }
    
    class func getPersianLocaleMonthName(_ monthOrdinal: Int) -> String {
        switch monthOrdinal {
        case 0:
            return "ژانویه"
        case 1:
            return "فوریه"
        case 2:
            return "مارس"
        case 3:
            return "آوریل"
        case 4:
            return "می"
        case 5:
            return "جون"
        case 6:
            return "جولای"
        case 7:
            return "آگوست"
        case 8:
            return "سپتامبر"
        case 9:
            return "اکتبر"
        case 10:
            return "نوامبر"
        case 11:
            return "دسامبر"
        default:
            return ""
        }
    }
    
    class func getRelativeDayName(_ date: Date?) -> String? {
        guard date != nil else {
            return nil;
        }
        let date1 = Calendar.current.startOfDay(for: Date())
        let date2 = Calendar.current.startOfDay(for: date!)
        
        let components = Calendar.current.dateComponents([.day], from: date1, to: date2)
        
        switch components.day! {
        case 0:
            return "امروز"
        case 1:
            return "فردا"
        case 2:
            return "پس‌فردا"
        case -1:
            return "دیروز"
        case -2:
            return "پریروز"
        default:
            return String(Calendar.current.component(.day, from: date!)) + "ام"
        }
    }
}
