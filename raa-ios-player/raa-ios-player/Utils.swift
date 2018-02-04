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
