//
//  LiveViewController.swift
//  raa-ios-player
//
//  Created by Hamid on 1/25/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation
import UIKit

class LiveBroadcastContainerViewController : PlayerViewController {
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
}

class LiveBroadcastViewController : UIViewController {
    
    @IBOutlet var programCardTableView: UITableView?
    
    struct Defaults {
        public static let CELL_HEIGHT: CGFloat = 150
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override func viewDidLoad() {
        programCardTableView?.dataSource = self
        programCardTableView?.delegate = self

        programCardTableView?.reloadData()
    }
}

extension LiveBroadcastViewController : UITableViewDelegate {
}

extension LiveBroadcastViewController : UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return Defaults.CELL_HEIGHT
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return 1
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        // Table view cells are reused and should be dequeued using a cell identifier.
        let cellIdentifier = "liveProgramCell"
        
        guard let cell = tableView.dequeueReusableCell(withIdentifier: cellIdentifier, for: indexPath) as? ProgramCardTableViewCell else {
            fatalError("The dequeued cell is not an instance of ProgramCardTableViewCell.")
        }
        
        let groupCardContent = storyboard?.instantiateViewController(withIdentifier: "ProgramContent")
        cell.card?.shouldPresent(groupCardContent, from: self)

        cell.card?.backgroundImage = #imageLiteral(resourceName: "default-thumbnail")
        
        // Fetches the appropriate meal for the data source layout.
        
        
        return cell
    }
}
