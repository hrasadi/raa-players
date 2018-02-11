//
//  ProgramInfoDirectoryManager.swift
//  raa-ios-player
//
//  Created by Hamid on 2/9/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation
import os
import UIKit

class ProgramInfoDirectoryManager : UICommunicator {
    static let PINFO_DIR_ENDPOINT = Context.SERVER_URL + "/programInfoDirectory"

    private var jsonDecoder = JSONDecoder()

    public var programInfoDirectory: ProgramInfoDirectory?
    
    override init() {
        super.init()
        
        self.loadProgramInfoDirectory()
    }

    // Load feed from server
    func loadProgramInfoDirectory() {
        let task = URLSession.shared.dataTask(with: URL(string: ProgramInfoDirectoryManager.PINFO_DIR_ENDPOINT)!) {
            data, response, error in
            guard error == nil else {
                os_log("Error while loading program info directory: %@", type: .error, error!.localizedDescription)
                return
            }
            os_log("Fetched public feed from server.", type: .default)
            
            guard data != nil else {
                return
            }
            
            self.programInfoDirectory = try! self.jsonDecoder.decode(ProgramInfoDirectory.self, from: data!)
            
            self.notifyModelUpdate()
        }
        task.resume()
    }
    
    public func preloadImages(_ completionHandler: @escaping () -> Void) {
        os_log("Preloading images from server...")
        
        if self.programInfoDirectory != nil {
            DispatchQueue.global().async {
                for pinfo in self.programInfoDirectory!.ProgramInfos {
                    pinfo.value.thumbnailImage = self.getImage(pinfo.value.Thumbnail, defaultImage: #imageLiteral(resourceName: "default-thumbnail"))
                    pinfo.value.bannerImage = self.getImage(pinfo.value.Thumbnail, defaultImage: #imageLiteral(resourceName: "default-banner"))
                }
                completionHandler()
            }
        }
    }
    
    private func getImage(_ urlString: String?, defaultImage: UIImage!) -> UIImage! {
        var result = defaultImage

        if urlString != nil {
            let url = URL(string: urlString!)!
            let data = try? Data(contentsOf: url)
            if (data != nil) {
                result = UIImage(data: data!)
            }
        }
        return result!
    }
    
    override func pullData() -> Any? {
        return self.programInfoDirectory
    }
}
