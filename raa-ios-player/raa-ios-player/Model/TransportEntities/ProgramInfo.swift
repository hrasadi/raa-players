//
//  ProgramInfo.swift
//  raa-ios-player
//
//  Created by Hamid on 2/9/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation
import UIKit

class ProgramInfo : Codable {
    var ProgramId: String! {
        didSet {
            if ProgramId == nil {
                ProgramId = ""
            }
        }
    }
    var Title: String! {
        didSet {
            if Title == nil {
                Title = ""
            }
        }
    }
    var About: String?
    var Thumbnail: String?
    var Banner: String?

    // Non-coding properties
    var thumbnailImage: UIImage? = nil
    var bannerImage: UIImage? = nil
    
    private enum CodingKeys: String, CodingKey {
        case ProgramId
        case Title
        case About
        case Thumbnail
        case Banner
    }
}

class ProgramInfoDirectory : Codable {
    var ProgramInfos: [String: ProgramInfo]
}
