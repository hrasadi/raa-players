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
    var ProgramId: String! = ""
    var Title: String! = ""
    var About: String?
    var Thumbnail: String?
    var Banner: String?

    private enum CodingKeys: String, CodingKey {
        case ProgramId
        case Title
        case About
        case Thumbnail
        case Banner
    }

    // Non-coding properties
    static let defaultThumbnailImageData: Data! = {
        return UIImagePNGRepresentation(#imageLiteral(resourceName: "default-thumbnail"))
    }()
    static let defaultBannerImageData: Data! = {
        return UIImagePNGRepresentation(#imageLiteral(resourceName: "default-banner"))
    }()

    var thumbnailImageData: Data? = nil
    var bannerImageData: Data? = nil
}

class ProgramInfoDirectory : Codable {
    var ProgramInfos: [String: ProgramInfo] = [:]
    var Archive: [String: String]?
}
